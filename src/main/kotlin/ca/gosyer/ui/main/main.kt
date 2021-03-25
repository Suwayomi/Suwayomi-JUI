/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import ca.gosyer.backend.network.networkModule
import ca.gosyer.backend.preferences.preferencesModule
import ca.gosyer.ui.base.vm.composeViewModel
import ca.gosyer.ui.base.vm.viewModelModule
import ca.gosyer.ui.categories.openCategoriesMenu
import ca.gosyer.ui.extensions.openExtensionsMenu
import ca.gosyer.ui.library.openLibraryMenu
import ca.gosyer.ui.sources.openSourcesMenu
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.system.userDataDir
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.koin.core.context.startKoin
import kotlin.concurrent.thread
import java.io.File

fun main() {
    GlobalScope.launch {
        val logger = KotlinLogging.logger("Server")
        val runtime = Runtime.getRuntime()

        val jarFile = File(userDataDir,"Tachidesk.jar")
        if (!jarFile.exists()) {
            logger.info { "Copying server to resources" }
            javaClass.getResourceAsStream("/Tachidesk.jar").buffered().use { input ->
                jarFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        logger.info { "Starting server" }
        val process = runtime.exec("""java -jar "${jarFile.absolutePath}"""")
        runtime.addShutdownHook(thread(start = false) {
            process?.destroy()
        })
        val reader = process.inputStream.reader().buffered()
        logger.info { "Server started successfully" }
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            logger.info { line }
        }
        logger.info { "Server closed" }
        val exitVal = process.waitFor()
        logger.info { "Process exitValue: $exitVal" }
    }

    startKoin {
        modules(
            preferencesModule,
            networkModule,
            viewModelModule
        )
    }


    ThemedWindow(title = "TachideskJUI") {
        val vm = composeViewModel<MainViewModel>()
        Column(Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
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