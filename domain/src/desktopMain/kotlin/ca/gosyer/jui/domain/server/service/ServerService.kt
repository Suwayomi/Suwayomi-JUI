/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.server.service

import ca.gosyer.jui.core.io.copyTo
import ca.gosyer.jui.core.io.userDataDir
import ca.gosyer.jui.core.lang.withIOContext
import ca.gosyer.jui.domain.build.BuildKonfig
import com.diamondedge.logging.logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import me.tatarka.inject.annotations.Inject
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import java.io.File.pathSeparatorChar
import java.io.IOException
import java.io.Reader
import java.util.jar.JarInputStream
import kotlin.concurrent.thread
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isExecutable

@OptIn(DelicateCoroutinesApi::class)
@Inject
class ServerService(
    private val serverHostPreferences: ServerHostPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val host = serverHostPreferences.host().stateIn(GlobalScope)
    private val _initialized = MutableStateFlow(
        if (host.value) {
            ServerResult.STARTING
        } else {
            ServerResult.UNUSED
        },
    )
    val initialized = _initialized.asStateFlow()
    private var process: Process? = null

    fun startAnyway() {
        _initialized.value = ServerResult.UNUSED
    }

    @Throws(IOException::class)
    private suspend fun copyJar(jarFile: Path) {
        javaClass.getResourceAsStream("/Tachidesk.jar")?.source()
            ?.copyTo(FileSystem.SYSTEM.sink(jarFile).buffer())
    }

    private fun getJavaFromPath(javaPath: Path): String? {
        val javaExeFile = javaPath.resolve("java.exe").toNioPath()
        val javaUnixFile = javaPath.resolve("java").toNioPath()
        return when {
            javaExeFile.exists() && javaExeFile.isExecutable() -> javaExeFile.absolutePathString()
            javaUnixFile.exists() && javaUnixFile.isExecutable() -> javaUnixFile.absolutePathString()
            else -> null
        }
    }

    private fun getRuntimeJava(): String? = System.getProperty("java.home")?.let { getJavaFromPath(it.toPath().resolve("bin")) }

    private fun getPossibleJava(): String? =
        System.getProperty("java.library.path")?.split(pathSeparatorChar)
            .orEmpty()
            .asSequence()
            .mapNotNull {
                val file = it.toPath()
                if (file.toString().contains("java") || file.toString().contains("jdk")) {
                    if (file.name.equals("bin", true)) {
                        file
                    } else {
                        file.resolve("bin")
                    }
                } else {
                    null
                }
            }
            .mapNotNull { getJavaFromPath(it) }
            .firstOrNull()

    private suspend fun runService() {
        process?.destroy()
        withIOContext {
            process?.waitFor()
        }
        _initialized.value = if (host.value) {
            ServerResult.STARTING
        } else {
            ServerResult.UNUSED
            return
        }

        val jarFile = userDataDir / "Tachidesk.jar"
        if (!FileSystem.SYSTEM.exists(jarFile)) {
            log.info { "Copying server to resources" }
            withIOContext { copyJar(jarFile) }
        } else {
            try {
                val jarVersion = withIOContext {
                    JarInputStream(FileSystem.SYSTEM.source(jarFile).buffer().inputStream()).use { jar ->
                        jar.manifest?.mainAttributes?.getValue("JUI-KEY")?.toIntOrNull()
                    }
                }

                if (jarVersion != BuildKonfig.SERVER_CODE) {
                    log.info { "Updating server file from resources" }
                    withIOContext { copyJar(jarFile) }
                }
            } catch (e: IOException) {
                log.error(e) {
                    "Error accessing server jar, cannot update server, ${BuildKonfig.NAME} may not work properly"
                }
            }
        }

        val javaPath = getRuntimeJava() ?: getPossibleJava() ?: "java"
        log.info { "Starting server with $javaPath" }
        val properties = serverHostPreferences.properties()
        log.info { "Using server properties:\n" + properties.joinToString(separator = "\n") }

        withIOContext {
            val reader: Reader
            process = ProcessBuilder(javaPath, *properties, "-jar", jarFile.toString())
                .redirectErrorStream(true)
                .start()
                .also {
                    reader = it.inputStream.reader()
                }
            log.info { "Server started successfully" }
            val log = logging("Server")
            reader.forEachLine {
                if (_initialized.value == ServerResult.STARTING) {
                    when {
                        it.contains("Javalin started") ->
                            _initialized.value = ServerResult.STARTED

                        it.contains("Javalin has stopped") ->
                            _initialized.value = ServerResult.FAILED
                    }
                }
                log.info { it }
            }
            if (_initialized.value == ServerResult.STARTING) {
                _initialized.value = ServerResult.FAILED
            }
            log.info { "Server closed" }
            val exitVal = process?.waitFor()
            log.info { "Process exitValue: $exitVal" }
            process = null
        }
    }

    fun startServer() {
        scope.coroutineContext.cancelChildren()
        host
            .mapLatest {
                runService()
            }
            .catch {
                log.error(it) { "Error launching Tachidesk.jar" }
                if (_initialized.value == ServerResult.STARTING || _initialized.value == ServerResult.STARTED) {
                    _initialized.value = ServerResult.FAILED
                }
            }
            .launchIn(scope)
    }

    init {
        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                process?.destroy()
                process = null
            },
        )
    }

    enum class ServerResult {
        UNUSED,
        STARTING,
        STARTED,
        FAILED,
    }

    private companion object {
        private val log = logging()
    }
}
