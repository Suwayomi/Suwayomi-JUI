/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import ca.gosyer.BuildConfig
import ca.gosyer.data.DataModule
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.categories.openCategoriesMenu
import ca.gosyer.ui.extensions.openExtensionsMenu
import ca.gosyer.ui.library.openLibraryMenu
import ca.gosyer.ui.sources.openSourcesMenu
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.system.userDataDir
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.apache.logging.log4j.core.config.Configurator
import toothpick.configuration.Configuration
import toothpick.ktp.KTP
import java.io.BufferedReader
import java.io.File
import kotlin.concurrent.thread

fun main() {
    val clazz = MainViewModel::class.java
    Configurator.initialize(
        null,
        clazz.classLoader,
        clazz.getResource("log4j2.xml")?.toURI()
    )

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


    ThemedWindow(title = "TachideskJUI") {
        val vm = viewModel<MainViewModel>()
        Surface {
            Column(Modifier.fillMaxSize()) {
                Button(
                    onClick = ::openExtensionsMenu
                ) {
                    Text("Extensions")
                }
                Button(
                    onClick = ::openSourcesMenu
                ) {
                    Text("Sources")
                }
                Button(
                    onClick = ::openLibraryMenu
                ) {
                    Text("Library")
                }
                Button(
                    onClick = ::openCategoriesMenu
                ) {
                    Text("Categories")
                }
            }
        }
    }
}