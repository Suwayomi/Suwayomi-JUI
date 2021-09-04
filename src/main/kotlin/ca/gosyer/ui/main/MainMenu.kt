/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DrawerValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Surface
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.ui.base.components.LocalMenuController
import ca.gosyer.ui.base.components.MenuController
import ca.gosyer.ui.base.components.withMenuController
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.downloads.DownloadsMenu
import ca.gosyer.ui.extensions.ExtensionsMenu
import ca.gosyer.ui.library.LibraryScreen
import ca.gosyer.ui.main.components.SideMenu
import ca.gosyer.ui.manga.MangaMenu
import ca.gosyer.ui.settings.SettingsAdvancedScreen
import ca.gosyer.ui.settings.SettingsAppearance
import ca.gosyer.ui.settings.SettingsBackupScreen
import ca.gosyer.ui.settings.SettingsBrowseScreen
import ca.gosyer.ui.settings.SettingsGeneralScreen
import ca.gosyer.ui.settings.SettingsLibraryScreen
import ca.gosyer.ui.settings.SettingsReaderScreen
import ca.gosyer.ui.settings.SettingsScreen
import ca.gosyer.ui.settings.SettingsServerScreen
import ca.gosyer.ui.sources.SourcesMenu
import ca.gosyer.ui.sources.settings.SourceSettingsMenu
import com.github.zsoltk.compose.router.Router
import com.github.zsoltk.compose.savedinstancestate.Bundle
import com.github.zsoltk.compose.savedinstancestate.BundleScope

const val SIDE_MENU_EXPAND_DURATION = 500

@Composable
fun MainMenu(rootBundle: Bundle) {
    val vm = viewModel<MainViewModel>()
    Surface {
        Router("TopLevel", vm.startScreen.toRoute()) { backStack ->
            val controller = remember {
                MenuController(backStack)
            }
            BoxWithConstraints {
                // if (maxWidth > 720.dp) {
                WideMainMenu(rootBundle, controller)
                // } else {
                // SkinnyMainMenu(rootBundle, controller)
                // }
            }
        }
    }
}

@Composable
fun SkinnyMainMenu(
    rootBundle: Bundle,
    controller: MenuController
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    LaunchedEffect(controller.sideMenuVisible) {
        if (controller.sideMenuVisible) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            controller.openSideMenu()
        } else {
            controller.closeSideMenu()
        }
    }
    DisposableEffect(Unit) {
        controller.setAsDrawer()
        onDispose {
            controller.setAsNotDrawer()
        }
    }

    ModalDrawer(
        {
            SideMenu(Modifier.fillMaxWidth(), controller)
        },
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen
    ) {
        withMenuController(controller) {
            MainWindow(Modifier, rootBundle)
        }
    }
}

@Composable
fun WideMainMenu(
    rootBundle: Bundle,
    controller: MenuController
) {
    Box {
        val startPadding by animateDpAsState(
            if (controller.sideMenuVisible) {
                200.dp
            } else {
                0.dp
            },
            animationSpec = tween(SIDE_MENU_EXPAND_DURATION)
        )
        if (startPadding != 0.dp) {
            SideMenu(Modifier.width(200.dp), controller)
        }
        withMenuController(controller) {
            MainWindow(Modifier.padding(start = startPadding), rootBundle)
        }
    }
}

@Composable
fun MainWindow(modifier: Modifier, rootBundle: Bundle) {
    Surface(Modifier.fillMaxSize().then(modifier)) {
        val menuController = LocalMenuController.current!!
        BundleScope("K${menuController.backStack.lastIndex}", rootBundle, false) {
            Crossfade(menuController.backStack.last()) { routing ->
                when (routing) {
                    is Routes.Library -> LibraryScreen {
                        menuController.push(Routes.Manga(it))
                    }
                    is Routes.Sources -> SourcesMenu(
                        {
                            menuController.push(Routes.SourceSettings(it))
                        }
                    ) {
                        menuController.push(Routes.Manga(it))
                    }
                    is Routes.Extensions -> ExtensionsMenu()
                    is Routes.Manga -> MangaMenu(routing.mangaId)
                    is Routes.Downloads -> DownloadsMenu()

                    is Routes.SourceSettings -> SourceSettingsMenu(routing.sourceId)

                    is Routes.Settings -> SettingsScreen(menuController)
                    is Routes.SettingsGeneral -> SettingsGeneralScreen(menuController)
                    is Routes.SettingsAppearance -> SettingsAppearance(menuController)
                    is Routes.SettingsServer -> SettingsServerScreen(menuController)
                    is Routes.SettingsLibrary -> SettingsLibraryScreen(menuController)
                    is Routes.SettingsReader -> SettingsReaderScreen(menuController)
                    /*is Route.SettingsDownloads -> SettingsDownloadsScreen(menuController)
                    is Route.SettingsTracking -> SettingsTrackingScreen(menuController)*/
                    is Routes.SettingsBrowse -> SettingsBrowseScreen(menuController)
                    is Routes.SettingsBackup -> SettingsBackupScreen(menuController)
                    /*is Route.SettingsSecurity -> SettingsSecurityScreen(menuController)
                    is Route.SettingsParentalControls -> SettingsParentalControlsScreen(menuController)*/
                    is Routes.SettingsAdvanced -> SettingsAdvancedScreen(menuController)
                }
            }
        }
        /*Box(Modifier.padding(bottom = 32.dp).align(Alignment.BottomCenter)) {
            val shape = RoundedCornerShape(50.dp)
            Box(
                Modifier
                    .width(200.dp)
                    .defaultMinSize(minHeight = 64.dp)
                    .shadow(4.dp, shape)
                    .background(SolidColor(Color.Gray), alpha = 0.2F)
                    .clip(shape),
                contentAlignment = Alignment.Center
            ) {
                Text("Test text")
            }
        }*/
    }
}
