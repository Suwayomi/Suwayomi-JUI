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
import java.util.jar.JarInputStream
import javax.inject.Inject
import kotlin.concurrent.thread

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
    private val runtime = Runtime.getRuntime()
    private var process: Process? = null

    fun startAnyway() {
        initialized.value = ServerResult.UNUSED
    }

    private fun copyJar(jarFile: File): Boolean {
        return javaClass.getResourceAsStream("/Tachidesk.jar")?.buffered()?.use { input ->
            jarFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }?.let { true } ?: false
    }

    init {
        runtime.addShutdownHook(
            thread(start = false) {
                process?.destroy()
                process = null
            }
        )
        host.onEach {
            process?.destroy()
            initialized.value = if (host.value) {
                ServerResult.STARTING
            } else {
                ServerResult.UNUSED
                return@onEach
            }
            GlobalScope.launch {

                val jarFile = File(userDataDir, "Tachidesk.jar")
                if (!jarFile.exists()) {
                    info { "Copying server to resources" }
                    if (!copyJar(jarFile)) {
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
                            copyJar(jarFile)
                        }
                    } catch (e: IOException) {
                        error(e) {
                            "Error accessing server jar, cannot update server, ${BuildConfig.NAME} may not work properly"
                        }
                    }
                }

                val javaLibraryPath = System.getProperty("java.library.path").substringBefore(File.pathSeparator)
                val javaExeFile = File(javaLibraryPath, "java.exe")
                val javaUnixFile = File(javaLibraryPath, "java")
                val javaExePath = when {
                    javaExeFile.exists() -> '"' + javaExeFile.absolutePath + '"'
                    javaUnixFile.exists() -> '"' + javaUnixFile.absolutePath + '"'
                    else -> "java"
                }

                info { "Starting server with $javaExePath" }
                val reader: BufferedReader
                process = runtime.exec("""$javaExePath -jar "${jarFile.absolutePath}"""").also {
                    reader = it.inputStream.bufferedReader()
                }
                info { "Server started successfully" }
                val logger = KotlinLogging.logger("Server")
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (initialized.value == ServerResult.STARTING) {
                        if (line?.contains("Javalin started") == true) {
                            initialized.value = ServerResult.STARTED
                        } else if (line?.contains("Javalin has stopped") == true) {
                            initialized.value = ServerResult.FAILED
                        }
                    }
                    logger.info { line }
                }
                if (initialized.value == ServerResult.STARTING) {
                    initialized.value = ServerResult.FAILED
                }
                info { "Server closed" }
                val exitVal = process?.waitFor()
                info { "Process exitValue: $exitVal" }
                process = null
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
