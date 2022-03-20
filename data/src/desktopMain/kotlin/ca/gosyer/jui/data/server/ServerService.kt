/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server

import ca.gosyer.jui.core.io.copyTo
import ca.gosyer.jui.core.io.userDataDir
import ca.gosyer.jui.core.lang.withIOContext
import ca.gosyer.jui.core.logging.CKLogger
import ca.gosyer.jui.data.build.BuildKonfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import mu.KotlinLogging
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import java.io.File.pathSeparatorChar
import java.io.IOException
import java.io.Reader
import java.util.jar.Attributes
import java.util.jar.JarInputStream
import kotlin.concurrent.thread
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isExecutable

@OptIn(DelicateCoroutinesApi::class)
class ServerService @Inject constructor(
    private val serverHostPreferences: ServerHostPreferences
) {
    private val restartServerFlow = MutableSharedFlow<Unit>()
    private val host = serverHostPreferences.host().stateIn(GlobalScope)
    private val _initialized = MutableStateFlow(
        if (host.value) {
            ServerResult.STARTING
        } else {
            ServerResult.UNUSED
        }
    )
    val initialized = _initialized.asStateFlow()
    private var process: Process? = null

    fun startAnyway() {
        _initialized.value = ServerResult.UNUSED
    }

    fun restartServer() {
        GlobalScope.launch { restartServerFlow.emit(Unit) }
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

    private fun getRuntimeJava(): String? {
        return System.getProperty("java.home")?.let { getJavaFromPath(it.toPath().resolve("bin")) }
    }

    private fun getPossibleJava(): String? {
        return System.getProperty("java.library.path")?.split(pathSeparatorChar)
            .orEmpty()
            .asSequence()
            .mapNotNull {
                val file = it.toPath()
                if (file.toString().contains("java") || file.toString().contains("jdk")) {
                    if (file.name.equals("bin", true)) {
                        file
                    } else file.resolve("bin")
                } else null
            }
            .mapNotNull { getJavaFromPath(it) }
            .firstOrNull()
    }

    init {
        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                process?.destroy()
                process = null
            }
        )

        merge(restartServerFlow, host).mapLatest {
            process?.destroy()
            process?.waitFor()
            _initialized.value = if (host.value) {
                ServerResult.STARTING
            } else {
                ServerResult.UNUSED
                return@mapLatest
            }
            val handler = CoroutineExceptionHandler { _, throwable ->
                error(throwable) { "Error launching Tachidesk.jar" }
                if (_initialized.value == ServerResult.STARTING || _initialized.value == ServerResult.STARTED) {
                    _initialized.value = ServerResult.FAILED
                }
            }
            GlobalScope.launch(handler) {
                val jarFile = userDataDir / "Tachidesk.jar"
                if (!FileSystem.SYSTEM.exists(jarFile)) {
                    info { "Copying server to resources" }
                    withIOContext { copyJar(jarFile) }
                } else {
                    try {
                        val jarVersion = withIOContext {
                            JarInputStream(FileSystem.SYSTEM.source(jarFile).buffer().inputStream()).use { jar ->
                                jar.manifest?.mainAttributes?.getValue(Attributes.Name.IMPLEMENTATION_VERSION)?.toIntOrNull()
                            }
                        }

                        if (jarVersion != BuildKonfig.SERVER_CODE) {
                            info { "Updating server file from resources" }
                            withIOContext { copyJar(jarFile) }
                        }
                    } catch (e: IOException) {
                        error(e) {
                            "Error accessing server jar, cannot update server, ${BuildKonfig.NAME} may not work properly"
                        }
                    }
                }

                val javaPath = getRuntimeJava() ?: getPossibleJava() ?: "java"
                info { "Starting server with $javaPath" }
                val properties = serverHostPreferences.properties()
                info { "Using server properties:\n" + properties.joinToString(separator = "\n") }

                withIOContext {
                    val reader: Reader
                    process = ProcessBuilder(javaPath, *properties, "-jar", jarFile.toString())
                        .redirectErrorStream(true)
                        .start()
                        .also {
                            reader = it.inputStream.reader()
                        }
                    info { "Server started successfully" }
                    val logger = KotlinLogging.logger("Server")
                    reader.useLines { lines ->
                        lines.forEach {
                            if (_initialized.value == ServerResult.STARTING) {
                                if (it.contains("Javalin started")) {
                                    _initialized.value = ServerResult.STARTED
                                } else if (it.contains("Javalin has stopped")) {
                                    _initialized.value = ServerResult.FAILED
                                }
                            }
                            logger.info { it }
                        }
                    }
                    if (_initialized.value == ServerResult.STARTING) {
                        _initialized.value = ServerResult.FAILED
                    }
                    info { "Server closed" }
                    val exitVal = process?.waitFor()
                    info { "Process exitValue: $exitVal" }
                    process = null
                }
            }
        }.launchIn(GlobalScope)
    }

    enum class ServerResult {
        UNUSED,
        STARTING,
        STARTED,
        FAILED;
    }

    private companion object : CKLogger({})
}
