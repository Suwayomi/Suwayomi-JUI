/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server

import ca.gosyer.BuildConfig
import ca.gosyer.util.lang.withIOContext
import ca.gosyer.util.system.CKLogger
import ca.gosyer.util.system.userDataDir
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.Reader
import java.util.jar.JarInputStream
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.jvm.Throws

@OptIn(DelicateCoroutinesApi::class)
class ServerService @Inject constructor(
    val serverPreferences: ServerPreferences
) {
    private val host = serverPreferences.host().stateIn(GlobalScope)
    val initialized = MutableStateFlow(
        if (host.value) {
            ServerResult.STARTING
        } else {
            ServerResult.UNUSED
        }
    )
    private var process: Process? = null

    fun startAnyway() {
        initialized.value = ServerResult.UNUSED
    }

    @Throws(IOException::class)
    private fun copyJar(jarFile: File): Boolean {
        return javaClass.getResourceAsStream("/Tachidesk.jar")?.buffered()?.use { input ->
            jarFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }?.let { true } ?: false
    }

    private fun getJavaFromPath(javaPath: File): String? {
        val javaExeFile = File(javaPath, "java.exe")
        val javaUnixFile = File(javaPath, "java")
        return when {
            javaExeFile.exists() && javaExeFile.canExecute() -> javaExeFile.absolutePath
            javaUnixFile.exists() && javaUnixFile.canExecute() -> javaUnixFile.absolutePath
            else -> null
        }
    }

    private fun getRuntimeJava(): String? {
        return System.getProperty("java.home")?.let { getJavaFromPath(File(it, "bin")) }
    }

    private fun getPossibleJava(): String? {
        return System.getProperty("java.library.path")?.split(File.pathSeparatorChar)
            .orEmpty()
            .asSequence()
            .mapNotNull {
                val file = File(it)
                if (file.absolutePath.contains("java") || file.absolutePath.contains("jdk")) {
                    if (file.name.equals("bin", true)) {
                        file
                    } else File(file, "bin")
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
        host.onEach { host ->
            process?.destroy()
            initialized.value = if (host) {
                ServerResult.STARTING
            } else {
                ServerResult.UNUSED
                return@onEach
            }
            val handler = CoroutineExceptionHandler { _, throwable ->
                error(throwable) { "Error launching Tachidesk.jar" }
                if (initialized.value == ServerResult.STARTING || initialized.value == ServerResult.STARTED) {
                    initialized.value = ServerResult.FAILED
                }
            }
            GlobalScope.launch(handler) {
                val jarFile = File(userDataDir, "Tachidesk.jar")
                if (!jarFile.exists()) {
                    info { "Copying server to resources" }
                    if (withIOContext { !copyJar(jarFile) }) {
                        initialized.value = ServerResult.NO_TACHIDESK_JAR
                    }
                } else {
                    try {
                        val jarVersion = withIOContext {
                            JarInputStream(jarFile.inputStream()).use { jar ->
                                jar.manifest?.mainAttributes?.getValue("Specification-Version")
                            }
                        }

                        if (jarVersion != BuildConfig.TACHIDESK_SP_VERSION) {
                            info { "Updating server file from resources" }
                            if (withIOContext { !copyJar(jarFile) }) {
                                initialized.value = ServerResult.NO_TACHIDESK_JAR
                            }
                        }
                    } catch (e: IOException) {
                        error(e) {
                            "Error accessing server jar, cannot update server, ${BuildConfig.NAME} may not work properly"
                        }
                    }
                }

                val javaPath = getRuntimeJava() ?: getPossibleJava() ?: "java"
                info { "Starting server with $javaPath" }

                withIOContext {
                    val reader: Reader
                    process = ProcessBuilder(javaPath, "-jar", jarFile.absolutePath)
                        .redirectErrorStream(true)
                        .start()
                        .also {
                            reader = it.inputStream.reader()
                        }
                    info { "Server started successfully" }
                    val logger = KotlinLogging.logger("Server")
                    reader.useLines { lines ->
                        lines.forEach {
                            if (initialized.value == ServerResult.STARTING) {
                                if (it.contains("Javalin started")) {
                                    initialized.value = ServerResult.STARTED
                                } else if (it.contains("Javalin has stopped")) {
                                    initialized.value = ServerResult.FAILED
                                }
                            }
                            logger.info { it }
                        }
                    }
                    if (initialized.value == ServerResult.STARTING) {
                        initialized.value = ServerResult.FAILED
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
        NO_TACHIDESK_JAR,
        STARTING,
        STARTED,
        FAILED;
    }

    private companion object : CKLogger({})
}
