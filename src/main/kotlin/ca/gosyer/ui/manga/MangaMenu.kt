/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import ca.gosyer.build.BuildConfig
import ca.gosyer.data.models.Category
import ca.gosyer.data.models.Manga
import ca.gosyer.ui.base.WindowDialog
import ca.gosyer.ui.base.components.ErrorScreen
import ca.gosyer.ui.base.components.KamelImage
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.LocalMenuController
import ca.gosyer.ui.base.components.MenuController
import ca.gosyer.ui.base.components.TextActionIcon
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.reader.openReaderMenu
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.lang.launchApplication
import com.google.accompanist.flowlayout.FlowRow
import io.kamel.image.lazyPainterResource
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

@OptIn(DelicateCoroutinesApi::class)
fun openMangaMenu(mangaId: Long) {
    launchApplication {
        ThemedWindow(::exitApplication, title = BuildConfig.NAME) {
            Surface {
                MangaMenu(mangaId)
            }
        }
    }
}

@Composable
fun MangaMenu(mangaId: Long, menuController: MenuController? = LocalMenuController.current) {
    val vm = viewModel<MangaMenuViewModel> {
        MangaMenuViewModel.Params(mangaId)
    }
    val manga by vm.manga.collectAsState()
    val chapters by vm.chapters.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val dateTimeFormatter by vm.dateTimeFormatter.collectAsState()
    val categoriesExist by vm.categoriesExist.collectAsState()

    LaunchedEffect(Unit) {
        vm.chooseCategoriesFlow.collect { (availableCategories, usedCategories) ->
            openCategorySelectDialog(availableCategories, usedCategories, vm::addFavorite)
        }
    }

    Box {
        Column {
            Toolbar(
                stringResource("location_manga"),
                menuController,
                menuController != null,
                actions = {
                    AnimatedVisibility(categoriesExist && manga?.inLibrary == true) {
                        TextActionIcon(
                            vm::setCategories,
                            stringResource("edit_categories"),
                            Icons.Rounded.Label
                        )
                    }
                    TextActionIcon(
                        vm::toggleFavorite,
                        stringResource(if (manga?.inLibrary == true) "action_remove_favorite" else "action_favorite"),
                        if (manga?.inLibrary == true) {
                            Icons.Rounded.Favorite
                        } else {
                            Icons.Rounded.FavoriteBorder
                        },
                        manga != null
                    )
                    TextActionIcon(
                        vm::refreshManga,
                        stringResource("action_refresh_manga"),
                        Icons.Rounded.Refresh,
                        !isLoading
                    )
                }
            )

            manga.let { manga ->
                if (manga != null) {
                    Box {
                        val state = rememberLazyListState()
                        LazyColumn(state = state) {
                            item {
                                MangaItem(manga)
                            }
                            if (chapters.isNotEmpty()) {
                                items(chapters) { chapter ->
                                    ChapterItem(
                                        chapter,
                                        dateTimeFormatter::format,
                                        onClick = { openReaderMenu(it, manga.id) },
                                        toggleRead = vm::toggleRead,
                                        toggleBookmarked = vm::toggleBookmarked,
                                        markPreviousAsRead = vm::markPreviousRead,
                                        onClickDownload = vm::downloadChapter,
                                        onClickDeleteChapter = vm::deleteDownload,
                                        onClickStopDownload = vm::stopDownloadingChapter
                                    )
                                }
                            } else if (!isLoading) {
                                item {
                                    ErrorScreen(
                                        stringResource("no_chapters_found"),
                                        Modifier.height(400.dp).fillMaxWidth(),
                                        retry = vm::loadChapters
                                    )
                                }
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            adapter = rememberScrollbarAdapter(state)
                        )
                    }
                } else if (!isLoading) {
                    ErrorScreen(stringResource("failed_manga_fetch"), retry = vm::loadManga)
                }
            }
        }
        if (isLoading) {
            LoadingScreen()
        }
    }
}

@Composable
fun MangaItem(manga: Manga) {
    BoxWithConstraints(Modifier.padding(8.dp)) {
        if (maxWidth > 600.dp) {
            Row {
                Cover(manga, Modifier.width(300.dp))
                Spacer(Modifier.width(16.dp))
                MangaInfo(manga)
            }
        } else {
            Column {
                Cover(manga, Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(16.dp))
                MangaInfo(manga)
            }
        }
    }
}

@Composable
private fun Cover(manga: Manga, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier then Modifier
            .padding(4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        KamelImage(
            lazyPainterResource(manga, filterQuality = FilterQuality.Medium),
            manga.title,
            Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MangaInfo(manga: Manga, modifier: Modifier = Modifier) {
    SelectionContainer {
        Column(modifier) {
            Text(manga.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (!manga.author.isNullOrEmpty()) {
                Text(manga.author, fontSize = 18.sp)
            }
            if (!manga.artist.isNullOrEmpty() && manga.artist != manga.author) {
                Text(manga.artist, fontSize = 18.sp)
            }
            if (!manga.description.isNullOrEmpty()) {
                Text(manga.description)
            }
            if (manga.genre.isNotEmpty()) {
                FlowRow {
                    manga.genre.fastForEach {
                        Chip(it)
                    }
                }
                // Text(manga.genre.joinToString())
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Box(Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
        Card(
            shape = RoundedCornerShape(50),
            elevation = 4.dp
        ) {
            Text(text, Modifier.align(Alignment.Center).padding(8.dp))
        }
    }
}

fun openCategorySelectDialog(
    categories: List<Category>,
    oldCategories: List<Category>,
    onPositiveClick: (List<Category>, List<Category>) -> Unit
) {
    val enabledCategoriesFlow = MutableStateFlow(oldCategories)
    WindowDialog(
        "Select Categories",
        onPositiveButton = { onPositiveClick(enabledCategoriesFlow.value, oldCategories) }
    ) {
        val enabledCategories by enabledCategoriesFlow.collectAsState()
        val state = rememberLazyListState()
        Box {
            LazyColumn(state = state) {
                items(categories) { category ->
                    Row(
                        Modifier.fillMaxWidth().padding(8.dp)
                            .clickable {
                                if (category in enabledCategories) {
                                    enabledCategoriesFlow.value -= category
                                } else {
                                    enabledCategoriesFlow.value += category
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category.name, style = MaterialTheme.typography.subtitle1)
                        Checkbox(
                            category in enabledCategories,
                            onCheckedChange = null
                        )
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                adapter = rememberScrollbarAdapter(state)
            )
        }
    }
}
