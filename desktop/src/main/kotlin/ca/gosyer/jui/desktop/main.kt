/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.desktop

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.configureSwingGlobalsForCompose
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.jui.core.io.userDataDir
import ca.gosyer.jui.core.lang.withUIContext
import ca.gosyer.jui.core.prefs.getAsFlow
import ca.gosyer.jui.desktop.build.BuildConfig
import ca.gosyer.jui.desktop.logging.initializeLogger
import ca.gosyer.jui.domain.server.service.ServerService.ServerResult
import ca.gosyer.jui.domain.ui.model.ThemeMode
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.base.theme.AppTheme
import ca.gosyer.jui.ui.main.MainMenu
import ca.gosyer.jui.ui.main.components.DebugOverlay
import ca.gosyer.jui.ui.main.components.Tray
import ca.gosyer.jui.ui.util.compose.WindowGet
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.prefs.asStateIn
import ca.gosyer.jui.uicore.resources.stringResource
import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.DarculaTheme
import com.github.weisj.darklaf.theme.IntelliJTheme
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.message
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme
import java.util.Locale
import kotlin.system.exitProcess

@OptIn(DelicateCoroutinesApi::class)
suspend fun main() {
    initializeLogger(userDataDir.resolve("logging"))

    if (BuildConfig.DEBUG) {
        System.setProperty("kotlinx.coroutines.debug", "on")
    }

    val appComponent = AppComponent.getInstance()
    appComponent.migrations.runMigrations()
    appComponent.appMigrations.runMigrations()

    val serverService = appComponent.serverService
    serverService.initialized
        .filter { it == ServerResult.STARTED || it == ServerResult.UNUSED }
        .onEach {
            appComponent.downloadService.init()
            // dataComponent.libraryUpdateService.init()
        }
        .launchIn(GlobalScope)

    val uiPreferences = appComponent.uiPreferences
    val uiHooks = appComponent.hooks

    // Call setDefault before getting a resource bundle
    val language = uiPreferences.language().get()
    if (language.isNotBlank()) {
        val locale: Locale? = Locale.forLanguageTag(language)
        if (locale != null) {
            Locale.setDefault(locale)
        }
    }

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
    ) = WindowGet.from(windowSettings.get())

    val confirmExit = uiPreferences.confirmExit().asStateIn(GlobalScope)

    val displayDebugInfoFlow = MutableStateFlow(false)

    awaitApplication {
        CompositionLocalProvider(*uiHooks) {
            // Exit the whole application when this window closes
            DisposableEffect(Unit) {
                onDispose {
                    exitProcess(0)
                }
            }
            val windowState = rememberWindowState(
                size = size,
                position = position,
                placement = placement
            )

            val icon = remember { StableHolder(MR.images.icon.image.toPainter()) }

            Tray(icon)

            val confirmExitDialogState = rememberMaterialDialogState()

            Window(
                onCloseRequest = {
                    if (confirmExit.value) {
                        confirmExitDialogState.show()
                    } else {
                        exitApplication()
                    }
                },
                title = BuildConfig.NAME,
                icon = icon.item,
                state = windowState,
                onKeyEvent = {
                    if (it.type == KeyEventType.KeyUp) {
                        when (it.key) {
                            Key.Home -> {
                                // backPressHandler.handle()
                                false
                            }
                            Key.F3 -> {
                                displayDebugInfoFlow.value = !displayDebugInfoFlow.value
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
            ) {
                LaunchedEffect(Unit) {
                    serverService.startServer()
                }
                AppTheme {
                    Crossfade(serverService.initialized.collectAsState().value) { initialized ->
                        when (initialized) {
                            ServerResult.STARTED, ServerResult.UNUSED -> {
                                Box {
                                    MainMenu()
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
                                        errorMessage = stringResource(MR.strings.unable_to_start_server),
                                        retryMessage = stringResource(MR.strings.action_start_anyway),
                                        retry = serverService::startAnyway
                                    )
                                }
                            }
                        }
                    }

                    MaterialDialog(
                        confirmExitDialogState,
                        buttons = {
                            positiveButton(stringResource(MR.strings.action_ok), onClick = ::exitApplication)
                            negativeButton(stringResource(MR.strings.action_cancel))
                        },
                        properties = getMaterialDialogProperties(
                            size = DpSize(400.dp, 200.dp)
                        )
                    ) {
                        title(stringResource(MR.strings.confirm_exit))
                        message(stringResource(MR.strings.confirm_exit_message))
                    }
                }
            }
        }
    }
}
