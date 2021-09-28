/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.configureSwingGlobalsForCompose
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.build.BuildConfig
import ca.gosyer.core.logging.initializeLogger
import ca.gosyer.data.DataModule
import ca.gosyer.data.server.ServerService
import ca.gosyer.data.server.ServerService.ServerResult
import ca.gosyer.data.translation.XmlResourceBundle
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.ui.model.ThemeMode
import ca.gosyer.ui.base.WindowDialog
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.LocalComposeWindow
import ca.gosyer.ui.base.components.setIcon
import ca.gosyer.ui.base.prefs.asStateIn
import ca.gosyer.ui.base.resources.LocalResources
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.theme.AppTheme
import ca.gosyer.util.lang.withUIContext
import ca.gosyer.util.system.getAsFlow
import ca.gosyer.util.system.userDataDir
import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.DarculaTheme
import com.github.weisj.darklaf.theme.IntelliJTheme
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.backpress.LocalBackPressHandler
import com.github.zsoltk.compose.savedinstancestate.Bundle
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme
import toothpick.configuration.Configuration
import toothpick.ktp.KTP
import toothpick.ktp.extension.getInstance
import java.util.Locale
import kotlin.system.exitProcess

@OptIn(DelicateCoroutinesApi::class)
suspend fun main() {
    initializeLogger(userDataDir.resolve("logging"))

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
    val uiPreferences = scope.getInstance<UiPreferences>()

    // Call setDefault before getting a resource bundle
    val language = uiPreferences.language().get()
    if (language.isNotBlank()) {
        val locale: Locale? = Locale.forLanguageTag(language)
        if (locale != null) {
            Locale.setDefault(locale)
        }
    }

    val resources = scope.getInstance<XmlResourceBundle>()

    // Set the Compose constants before any
    // Swing functions are called
    configureSwingGlobalsForCompose()

    uiPreferences.themeMode()
        .getAsFlow {
            if (!uiPreferences.windowDecorations().get()) {
                return@getAsFlow
            }
            val theme = when (it) {
                ThemeMode.System -> when (currentSystemTheme) {
                    SystemTheme.LIGHT, SystemTheme.UNKNOWN -> IntelliJTheme()
                    SystemTheme.DARK -> DarculaTheme()
                }
                ThemeMode.Light -> IntelliJTheme()
                ThemeMode.Dark -> DarculaTheme()
            }
            withUIContext {
                LafManager.install(theme)
            }
        }
        .launchIn(GlobalScope)

    val windowSettings = uiPreferences.window()
    val (
        position,
        size,
        placement
    ) = windowSettings.get().get()

    val confirmExit = uiPreferences.confirmExit().asStateIn(GlobalScope)

    val displayDebugInfoFlow = MutableStateFlow(false)

    awaitApplication {
        // Exit the whole application when this window closes
        DisposableEffect(Unit) {
            onDispose {
                exitProcess(0)
            }
        }

        val backPressHandler = remember { BackPressHandler() }

        val rootBundle = remember { Bundle() }
        val windowState = rememberWindowState(
            size = size,
            position = position,
            placement = placement
        )

        Window(
            onCloseRequest = {
                if (confirmExit.value) {
                    WindowDialog(
                        title = resources.getStringA("confirm_exit"),
                        onPositiveButton = ::exitApplication
                    ) {
                        Text(stringResource("confirm_exit_message"))
                    }
                } else {
                    exitApplication()
                }
            },
            title = BuildConfig.NAME,
            state = windowState,
            onKeyEvent = {
                if (it.type == KeyEventType.KeyUp) {
                    when (it.key) {
                        Key.Home -> {
                            backPressHandler.handle()
                        }
                        Key.F3 -> {
                            displayDebugInfoFlow.value = !displayDebugInfoFlow.value
                            true
                        }
                        else -> false
                    }
                } else false
            }
        ) {
            setIcon()
            AppTheme {
                CompositionLocalProvider(
                    LocalComposeWindow provides window,
                    LocalBackPressHandler provides backPressHandler,
                    LocalResources provides resources
                ) {
                    Crossfade(serverService.initialized.collectAsState().value) { initialized ->
                        when (initialized) {
                            ServerResult.STARTED, ServerResult.UNUSED -> {
                                Box {
                                    MainMenu(rootBundle)
                                    val displayDebugInfo by displayDebugInfoFlow.collectAsState()
                                    if (displayDebugInfo) {
                                        DebugOverlay()
                                    }
                                }
                            }
                            ServerResult.STARTING, ServerResult.FAILED -> {
                                Surface {
                                    LoadingScreen(
                                        initialized == ServerResult.STARTING,
                                        errorMessage = stringResource("unable_to_start_server"),
                                        retryMessage = stringResource("action_start_anyway"),
                                        retry = serverService::startAnyway
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
