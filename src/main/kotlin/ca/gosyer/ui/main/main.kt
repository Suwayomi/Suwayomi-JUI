/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.WindowEvents
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.key.Key
import ca.gosyer.BuildConfig
import ca.gosyer.data.DataModule
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.system.userDataDir
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.backpress.LocalBackPressHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.apache.logging.log4j.core.config.Configurator
import toothpick.configuration.Configuration
import toothpick.ktp.KTP
import java.io.BufferedReader
import java.io.File
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

fun main() {
    val clazz = MainViewModel::class.java
    Configurator.initialize(
        null,
        clazz.classLoader,
        clazz.getResource("log4j2.xml")?.toURI()
    )
    val serverInitialized = MutableStateFlow(false)

    GlobalScope.launch {
        val logger = KotlinLogging.logger("Server")
        val runtime = Runtime.getRuntime()

        val jarFile = File(userDataDir,"Tachidesk.jar")
        if (!jarFile.exists()) {
            logger.info { "Copying server to resources" }
            javaClass.getResourceAsStream("/Tachidesk.jar")?.buffered()?.use { input ->
                jarFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        val javaLibraryPath = System.getProperty("java.library.path").substringBefore(File.pathSeparator)
        val javaExeFile = File(javaLibraryPath, "java.exe")
        val javaUnixFile = File(javaLibraryPath, "java")
        val javaExePath = when {
            javaExeFile.exists() ->'"' + javaExeFile.absolutePath + '"'
            javaUnixFile.exists() -> '"' + javaUnixFile.absolutePath + '"'
            else -> "java"
        }

        logger.info { "Starting server with $javaExePath" }
        val reader: BufferedReader
        val process = runtime.exec("""$javaExePath -jar "${jarFile.absolutePath}"""").also {
            reader = it.inputStream.bufferedReader()
        }
        runtime.addShutdownHook(thread(start = false) {
            process?.destroy()
        })
        logger.info { "Server started successfully" }
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            if (!serverInitialized.value && line?.contains("Javalin started") == true) {
                serverInitialized.value = true
            }
            logger.info { line }
        }
        logger.info { "Server closed" }
        val exitVal = process.waitFor()
        logger.info { "Process exitValue: $exitVal" }
    }

    if (BuildConfig.DEBUG) {
        System.setProperty("kotlinx.coroutines.debug", "on")
    }

    KTP.setConfiguration(
        if (BuildConfig.DEBUG) {
            Configuration.forDevelopment()
        } else {
            Configuration.forProduction()
        }
    )

    KTP.openRootScope()
        .installModules(
            DataModule
        )

    SwingUtilities.invokeLater {
        val window = AppWindow(
            title = BuildConfig.NAME
        )
        val backPressHandler = BackPressHandler()
        window.keyboard.setShortcut(Key.Home) {
            backPressHandler.handle()
        }

        window.show {
            DesktopMaterialTheme {
                CompositionLocalProvider(
                    LocalBackPressHandler provides backPressHandler
                ) {
                    val initialized by serverInitialized.collectAsState()
                    if (initialized) {
                        MainMenu()
                    } else {
                        LoadingScreen()
                    }
                }
            }
        }
    }
}