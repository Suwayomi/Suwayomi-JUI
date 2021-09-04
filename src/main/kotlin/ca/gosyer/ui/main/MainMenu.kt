/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.build.BuildConfig
import ca.gosyer.data.download.DownloadService
import ca.gosyer.data.ui.model.StartScreen
import ca.gosyer.ui.base.components.LocalMenuController
import ca.gosyer.ui.base.components.MenuController
import ca.gosyer.ui.base.components.combinedMouseClickable
import ca.gosyer.ui.base.components.withMenuController
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.downloads.DownloadsMenu
import ca.gosyer.ui.downloads.DownloadsMenuViewModel
import ca.gosyer.ui.extensions.ExtensionsMenu
import ca.gosyer.ui.library.LibraryScreen
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
            Box {
                val controller = remember {
                    MenuController(backStack)
                }
                val startPadding by animateDpAsState(
                    if (controller.sideMenuVisible) {
                        200.dp
                    } else {
                        0.dp
                    },
                    animationSpec = tween(SIDE_MENU_EXPAND_DURATION)
                )
                if (startPadding != 0.dp) {
                    SideMenu(controller)
                }
                withMenuController(controller) {
                    MainWindow(Modifier.padding(start = startPadding), rootBundle)
                }
            }
        }
    }
}

@Composable
fun SideMenu(controller: MenuController) {
    Surface(Modifier.width(200.dp).fillMaxHeight(), elevation = 2.dp) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                Row(
                    Modifier.fillMaxWidth().height(60.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        BuildConfig.NAME,
                        fontSize = 24.sp,
                        modifier = Modifier
                    )
                    IconButton(controller::closeSideMenu) {
                        Icon(Icons.Rounded.Close, contentDescription = null)
                    }
                }
                Spacer(Modifier.height(20.dp))
                remember { TopLevelMenus.values().filter { it.top } }.forEach { topLevelMenu ->
                    SideMenuItem(
                        topLevelMenu.isSelected(controller.backStack),
                        topLevelMenu,
                        controller.backStack::newRoot
                    )
                }
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.align(Alignment.BottomStart).padding(bottom = 8.dp)) {
                        remember { TopLevelMenus.values().filterNot { it.top } }.forEach { topLevelMenu ->
                            SideMenuItem(
                                topLevelMenu.isSelected(controller.backStack),
                                topLevelMenu,
                                controller.backStack::newRoot
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SideMenuItem(selected: Boolean, topLevelMenu: TopLevelMenus, newRoot: (Routes) -> Unit) {
    MainMenuItem(
        selected,
        stringResource(topLevelMenu.textKey),
        topLevelMenu.menu,
        topLevelMenu.selectedIcon,
        topLevelMenu.unselectedIcon,
        topLevelMenu.openInNewWindow,
        topLevelMenu.extraInfo,
        newRoot
    )
}

@Composable
fun MainWindow(modifier: Modifier, rootBundle: Bundle) {
    Surface(Modifier.fillMaxSize().then(modifier)) {
        val backStack = LocalMenuController.current!!.backStack
        BundleScope("K${backStack.lastIndex}", rootBundle, false) {
            Crossfade(backStack.last()) { routing ->
                when (routing) {
                    is Routes.Library -> LibraryScreen {
                        backStack.push(Routes.Manga(it))
                    }
                    is Routes.Sources -> SourcesMenu(
                        {
                            backStack.push(Routes.SourceSettings(it))
                        }
                    ) {
                        backStack.push(Routes.Manga(it))
                    }
                    is Routes.Extensions -> ExtensionsMenu()
                    is Routes.Manga -> MangaMenu(routing.mangaId, backStack)
                    is Routes.Downloads -> DownloadsMenu()

                    is Routes.SourceSettings -> SourceSettingsMenu(routing.sourceId, backStack)

                    is Routes.Settings -> SettingsScreen(backStack)
                    is Routes.SettingsGeneral -> SettingsGeneralScreen(backStack)
                    is Routes.SettingsAppearance -> SettingsAppearance(backStack)
                    is Routes.SettingsServer -> SettingsServerScreen(backStack)
                    is Routes.SettingsLibrary -> SettingsLibraryScreen(backStack)
                    is Routes.SettingsReader -> SettingsReaderScreen(backStack)
                    /*is Route.SettingsDownloads -> SettingsDownloadsScreen(backStack)
                    is Route.SettingsTracking -> SettingsTrackingScreen(backStack)*/
                    is Routes.SettingsBrowse -> SettingsBrowseScreen(backStack)
                    is Routes.SettingsBackup -> SettingsBackupScreen(backStack)
                    /*is Route.SettingsSecurity -> SettingsSecurityScreen(backStack)
                    is Route.SettingsParentalControls -> SettingsParentalControlsScreen(backStack)*/
                    is Routes.SettingsAdvanced -> SettingsAdvancedScreen(backStack)
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

@Composable
fun MainMenuItem(
    selected: Boolean,
    text: String,
    menu: Routes,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onMiddleClick: () -> Unit,
    extraInfo: (@Composable () -> Unit)? = null,
    onClick: (Routes) -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        backgroundColor = if (!selected) {
            Color.Transparent
        } else {
            MaterialTheme.colors.primary.copy(0.30F)
        },
        elevation = 0.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .height(40.dp)
                .combinedMouseClickable(
                    onClick = { onClick(menu) },
                    onMiddleClick = { onMiddleClick() }
                )
        ) {
            Spacer(Modifier.width(16.dp))
            Image(
                if (selected) {
                    selectedIcon
                } else {
                    unselectedIcon
                },
                text,
                Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text, color = MaterialTheme.colors.onSurface)
                if (extraInfo != null) {
                    extraInfo()
                }
            }
        }
    }
}

fun StartScreen.toRoute() = when (this) {
    StartScreen.Library -> Routes.Library
    StartScreen.Sources -> Routes.Sources
    StartScreen.Extensions -> Routes.Extensions
}

@Composable
fun DownloadsExtraInfo() {
    val vm = viewModel<DownloadsMenuViewModel>()
    val status by vm.serviceStatus.collectAsState()
    val list by vm.downloadQueue.collectAsState()
    val text = when (status) {
        DownloadService.Status.STARTING -> stringResource("downloads_loading")
        DownloadService.Status.RUNNING -> {
            if (list.isNotEmpty()) {
                stringResource("downloads_remaining", list.size)
            } else null
        }
        DownloadService.Status.STOPPED -> null
    }
    if (text != null) {
        Text(
            text,
            style = MaterialTheme.typography.body2,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
        )
    } else if (status == DownloadService.Status.STOPPED) {
        Surface(onClick = vm::restartDownloader, shape = RoundedCornerShape(4.dp)) {
            Text(
                stringResource("downloads_stopped"),
                style = MaterialTheme.typography.body2,
                color = Color.Red.copy(alpha = ContentAlpha.disabled)
            )
        }
    }
}
