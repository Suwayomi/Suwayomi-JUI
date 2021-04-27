/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.desktop.AppWindow
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.key.Key
import ca.gosyer.BuildConfig
import ca.gosyer.data.DataModule
import ca.gosyer.data.server.ServerService
import ca.gosyer.data.server.ServerService.ServerResult
import ca.gosyer.ui.base.components.ErrorScreen
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.theme.AppTheme
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.backpress.LocalBackPressHandler
import org.apache.logging.log4j.core.config.Configurator
import toothpick.configuration.Configuration
import toothpick.ktp.KTP
import toothpick.ktp.extension.getInstance
import javax.swing.SwingUtilities

fun main() {
    val clazz = MainViewModel::class.java
    Configurator.initialize(
        null,
        clazz.classLoader,
        clazz.getResource("log4j2.xml")?.toURI()
    )

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

    val scope = KTP.openRootScope()
        .installModules(
            DataModule
        )

    val serverService = scope.getInstance<ServerService>()

    SwingUtilities.invokeLater {
        val window = AppWindow(
            title = BuildConfig.NAME
        )
        val backPressHandler = BackPressHandler()
        window.keyboard.setShortcut(Key.Home) {
            backPressHandler.handle()
        }

        window.show {
            AppTheme {
                CompositionLocalProvider(
                    LocalBackPressHandler provides backPressHandler
                ) {
                    val initialized by serverService.initialized.collectAsState()
                    if (initialized == ServerResult.STARTED || initialized == ServerResult.UNUSED) {
                        MainMenu()
                    } else if (initialized == ServerResult.STARTING) {
                        LoadingScreen()
                    } else if (initialized == ServerResult.FAILED) {
                        ErrorScreen("Unable to start server")
                    }
                }
            }
        }
    }
}
