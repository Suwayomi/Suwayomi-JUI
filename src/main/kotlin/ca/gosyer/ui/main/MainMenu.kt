/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.foundation.Image
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
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Store
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
import ca.gosyer.BuildConfig
import ca.gosyer.data.ui.model.StartScreen
import ca.gosyer.ui.base.components.combinedMouseClickable
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.downloads.DownloadsMenu
import ca.gosyer.ui.downloads.DownloadsMenuViewModel
import ca.gosyer.ui.extensions.ExtensionsMenu
import ca.gosyer.ui.extensions.openExtensionsMenu
import ca.gosyer.ui.library.LibraryScreen
import ca.gosyer.ui.library.openLibraryMenu
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
import ca.gosyer.ui.sources.openSourcesMenu
import com.github.zsoltk.compose.router.BackStack
import com.github.zsoltk.compose.router.Router
import com.github.zsoltk.compose.savedinstancestate.Bundle
import com.github.zsoltk.compose.savedinstancestate.BundleScope

@Composable
fun MainMenu(rootBundle: Bundle) {
    val vm = viewModel<MainViewModel>()
    Surface {
        Router("TopLevel", vm.startScreen.toRoute()) { backStack ->
            Row {
                SideMenu(backStack)
                MainWindow(rootBundle, backStack)
            }
        }
    }
}

@Composable
fun SideMenu(backStack: BackStack<Route>) {
    Surface(Modifier.width(200.dp).fillMaxHeight(), elevation = 2.dp) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().height(60.dp)) {
                    Text(
                        BuildConfig.NAME,
                        fontSize = 30.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(Modifier.height(20.dp))
                remember { TopLevelMenus.values().filter { it.top } }.forEach { topLevelMenu ->
                    SideMenuItem(
                        topLevelMenu,
                        backStack
                    )
                }
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.align(Alignment.BottomStart).padding(bottom = 8.dp)) {
                        remember { TopLevelMenus.values().filterNot { it.top } }.forEach { topLevelMenu ->
                            SideMenuItem(
                                topLevelMenu,
                                backStack
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SideMenuItem(topLevelMenu: TopLevelMenus, backStack: BackStack<Route>) {
    MainMenuItem(
        backStack.elements.first() == topLevelMenu.menu,
        topLevelMenu.text,
        topLevelMenu.menu,
        topLevelMenu.selectedIcon,
        topLevelMenu.unselectedIcon,
        topLevelMenu.openInNewWindow,
        topLevelMenu.extraInfo
    ) {
        backStack.newRoot(it)
    }
}

@Composable
fun MainWindow(rootBundle: Bundle, backStack: BackStack<Route>) {
    Column(Modifier.fillMaxSize()) {
        BundleScope("K${backStack.lastIndex}", rootBundle, false) {
            when (val routing = backStack.last()) {
                is Route.Library -> LibraryScreen {
                    backStack.push(Route.Manga(it))
                }
                is Route.Sources -> SourcesMenu {
                    backStack.push(Route.Manga(it))
                }
                is Route.Extensions -> ExtensionsMenu()
                is Route.Manga -> MangaMenu(routing.mangaId, backStack)
                is Route.Downloads -> DownloadsMenu()

                is Route.Settings -> SettingsScreen(backStack)
                is Route.SettingsGeneral -> SettingsGeneralScreen(backStack)
                is Route.SettingsAppearance -> SettingsAppearance(backStack)
                is Route.SettingsServer -> SettingsServerScreen(backStack)
                is Route.SettingsLibrary -> SettingsLibraryScreen(backStack)
                is Route.SettingsReader -> SettingsReaderScreen(backStack)
                /*is Route.SettingsDownloads -> SettingsDownloadsScreen(backStack)
                is Route.SettingsTracking -> SettingsTrackingScreen(backStack)*/
                is Route.SettingsBrowse -> SettingsBrowseScreen(backStack)
                is Route.SettingsBackup -> SettingsBackupScreen(backStack)
                /*is Route.SettingsSecurity -> SettingsSecurityScreen(backStack)
                is Route.SettingsParentalControls -> SettingsParentalControlsScreen(backStack)*/
                is Route.SettingsAdvanced -> SettingsAdvancedScreen(backStack)
            }
        }
    }
}

@Composable
fun MainMenuItem(
    selected: Boolean,
    text: String,
    menu: Route,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onMiddleClick: () -> Unit,
    extraInfo: (@Composable () -> Unit)? = null,
    onClick: (Route) -> Unit
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
    StartScreen.Library -> Route.Library
    StartScreen.Sources -> Route.Sources
    StartScreen.Extensions -> Route.Extensions
}

@Composable
fun DownloadsExtraInfo() {
    val vm = viewModel<DownloadsMenuViewModel>()
    val list by vm.downloadQueue.collectAsState()
    if (list.isNotEmpty()) {
        Text(
            "${list.size} remaining",
            style = MaterialTheme.typography.body2,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
        )
    }
}

enum class TopLevelMenus(
    val text: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val menu: Route,
    val top: Boolean,
    val openInNewWindow: () -> Unit = {},
    val extraInfo: (@Composable () -> Unit)? = null
) {
    Library("Library", Icons.Outlined.Book, Icons.Filled.Book, Route.Library, true, ::openLibraryMenu),
    Sources("Sources", Icons.Outlined.Explore, Icons.Filled.Explore, Route.Sources, true, ::openSourcesMenu),
    Extensions("Extensions", Icons.Outlined.Store, Icons.Filled.Store, Route.Extensions, true, ::openExtensionsMenu),
    Downloads("Downloads", Icons.Outlined.Download, Icons.Filled.Download, Route.Downloads, false, extraInfo = { DownloadsExtraInfo() }),
    Settings("Settings", Icons.Outlined.Settings, Icons.Filled.Settings, Route.Settings, false)
}

sealed class Route {
    object Library : Route()
    object Sources : Route()
    object Extensions : Route()
    data class Manga(val mangaId: Long) : Route()
    object Downloads : Route()

    object Settings : Route()
    object SettingsGeneral : Route()
    object SettingsAppearance : Route()
    object SettingsLibrary : Route()
    object SettingsReader : Route()

    /*object SettingsDownloads : Route()
    object SettingsTracking : Route()*/
    object SettingsBrowse : Route()
    object SettingsBackup : Route()
    object SettingsServer : Route()

    /*object SettingsSecurity : Route()
    object SettingsParentalControls : Route()*/
    object SettingsAdvanced : Route()
}
