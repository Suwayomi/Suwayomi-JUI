/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.BuildConfig
import ca.gosyer.ui.base.vm.viewModel
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
import com.github.zsoltk.compose.router.Router
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.Bookmark
import compose.icons.fontawesomeicons.regular.Compass
import compose.icons.fontawesomeicons.regular.Edit
import compose.icons.fontawesomeicons.regular.Map

@Composable
fun MainMenu() {
    val vm = viewModel<MainViewModel>()
    Surface {
        Router<Route>("TopLevel", Route.Library) { backStack ->
            Row {
                Surface(elevation = 2.dp) {
                    Column(Modifier.width(200.dp).fillMaxHeight(),) {
                        Box(Modifier.fillMaxWidth().height(60.dp)) {
                            Text(BuildConfig.NAME, fontSize = 30.sp, modifier = Modifier.align(Alignment.Center))
                        }
                        Spacer(Modifier.height(20.dp))
                        remember { TopLevelMenus.values() }.forEach { topLevelMenu ->
                            MainMenuItem(topLevelMenu, backStack.elements.first() == topLevelMenu.menu) {
                                backStack.newRoot(it)
                            }
                        }

                        /*Button(
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
                        }*/
                    }
                }

                Column(Modifier.fillMaxSize()) {
                    when (val routing = backStack.last()) {
                        is Route.Library -> LibraryScreen {
                            backStack.push(Route.Manga(it))
                        }
                        is Route.Sources -> SourcesMenu {
                            backStack.push(Route.Manga(it))
                        }
                        is Route.Extensions -> ExtensionsMenu()
                        is Route.Manga -> MangaMenu(routing.mangaId, backStack)

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
    }
}

@Composable
fun MainMenuItem(menu: TopLevelMenus, selected: Boolean, onClick: (Route) -> Unit) {
    Card(
        Modifier.clickable { onClick(menu.menu) }.fillMaxWidth().height(40.dp),
        backgroundColor = if (!selected) {
            Color.Transparent
        } else {
            MaterialTheme.colors.primary.copy(0.30F)
        },
        contentColor = Color.Transparent,
        elevation = 0.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.width(16.dp))
            Image(menu.icon, menu.text, modifier = Modifier.size(20.dp), colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface))
            Spacer(Modifier.width(16.dp))
            Text(menu.text, color = MaterialTheme.colors.onSurface)
        }
    }
}

enum class TopLevelMenus(val text: String, val icon: ImageVector, val menu: Route) {
    Library("Library", FontAwesomeIcons.Regular.Bookmark, Route.Library),
    Sources("Sources", FontAwesomeIcons.Regular.Compass, Route.Sources),
    Extensions("Extensions", FontAwesomeIcons.Regular.Map, Route.Extensions),
    Settings("Settings", FontAwesomeIcons.Regular.Edit, Route.Settings)
}

sealed class Route {
    object Library : Route()
    object Sources : Route()
    object Extensions : Route()
    data class Manga(val mangaId: Long) : Route()

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
