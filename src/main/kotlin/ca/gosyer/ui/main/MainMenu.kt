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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.BuildConfig
import ca.gosyer.data.models.Manga
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.extensions.ExtensionsMenu
import ca.gosyer.ui.library.LibraryScreen
import ca.gosyer.ui.manga.MangaMenu
import ca.gosyer.ui.sources.SourcesMenu
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.backpress.LocalBackPressHandler
import com.github.zsoltk.compose.router.Router
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.Bookmark
import compose.icons.fontawesomeicons.regular.Compass
import compose.icons.fontawesomeicons.regular.Map

@Composable
fun MainMenu() {
    val vm = viewModel<MainViewModel>()
    val menu by vm.menu.collectAsState()
    Surface {

        Router<Routing>("TopLevel", Routing.LibraryMenu) { backStack ->
            Row {
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

                Column(Modifier.fillMaxSize()) {
                    when (val routing = backStack.last()) {
                        is Routing.LibraryMenu -> LibraryScreen {
                            backStack.push(Routing.MangaMenu(it))
                        }
                        is Routing.SourcesMenu -> SourcesMenu {
                            backStack.push(Routing.MangaMenu(it))
                        }
                        is Routing.ExtensionsMenu -> ExtensionsMenu()
                        is Routing.MangaMenu -> MangaMenu(routing.mangaId, backStack)
                    }
                }
            }
        }

    }
}

@Composable
fun MainMenuItem(menu: TopLevelMenus, selected: Boolean, onClick: (Routing) -> Unit) {
    Card(
        backgroundColor = if (!selected) {
            Color.Transparent
        } else {
           MaterialTheme.colors.primary.copy(0.30F)
        },
        contentColor = Color.Transparent,
        elevation = 0.dp
    ) {
        TextButton(
            onClick = { onClick(menu.menu) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Image(menu.icon, menu.text, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(menu.text)
            }
        }
    }
}

enum class TopLevelMenus(val text: String, val icon: ImageVector, val menu: Routing) {
    Library("Library", FontAwesomeIcons.Regular.Bookmark, Routing.LibraryMenu),
    Sources("Sources", FontAwesomeIcons.Regular.Compass, Routing.SourcesMenu),
    Extensions("Extensions", FontAwesomeIcons.Regular.Map, Routing.ExtensionsMenu);
}

sealed class Routing {
    object LibraryMenu : Routing()
    object SourcesMenu : Routing()
    object ExtensionsMenu : Routing()
    data class MangaMenu(val mangaId: Long): Routing()
}