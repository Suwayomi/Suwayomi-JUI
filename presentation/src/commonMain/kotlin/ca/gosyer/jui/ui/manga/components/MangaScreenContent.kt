/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.manga.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FlipToBack
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RemoveDone
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadState
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.base.navigation.Action
import ca.gosyer.jui.ui.base.navigation.ActionGroup
import ca.gosyer.jui.ui.base.navigation.ActionItem
import ca.gosyer.jui.ui.base.navigation.BackHandler
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.navigation.ToolbarDefault
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.ui.reader.rememberReaderLauncher
import ca.gosyer.jui.uicore.components.BottomActionItem
import ca.gosyer.jui.uicore.components.BottomActionMenu
import ca.gosyer.jui.uicore.components.ErrorScreen
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.icons.JuiAssets
import ca.gosyer.jui.uicore.icons.juiassets.DonePrev
import ca.gosyer.jui.uicore.resources.stringResource
import cafe.adriel.voyager.navigator.LocalNavigator
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant

@Composable
fun MangaScreenContent(
    isLoading: Boolean,
    manga: Manga?,
    chapters: ImmutableList<ChapterDownloadItem>,
    dateTimeFormatter: (Instant) -> String,
    categoriesExist: Boolean,
    chooseCategoriesFlowHolder: StableHolder<SharedFlow<Unit>>,
    availableCategories: ImmutableList<Category>,
    mangaCategories: ImmutableList<Category>,
    inActionMode: Boolean,
    selectedItems: ImmutableList<ChapterDownloadItem>,
    addFavorite: (List<Category>, List<Category>) -> Unit,
    setCategories: () -> Unit,
    toggleFavorite: () -> Unit,
    refreshManga: () -> Unit,
    downloadNext: (Int) -> Unit,
    downloadUnread: () -> Unit,
    downloadAll: () -> Unit,
    markRead: (Long?) -> Unit,
    markUnread: (Long?) -> Unit,
    bookmarkChapter: (Long?) -> Unit,
    unBookmarkChapter: (Long?) -> Unit,
    markPreviousRead: (Int) -> Unit,
    downloadChapter: (Int) -> Unit,
    deleteDownload: (Long?) -> Unit,
    stopDownloadingChapter: (Int) -> Unit,
    onSelectChapter: (Long) -> Unit,
    onUnselectChapter: (Long) -> Unit,
    selectAll: () -> Unit,
    invertSelection: () -> Unit,
    clearSelection: () -> Unit,
    downloadChapters: () -> Unit,
    loadChapters: () -> Unit,
    loadManga: () -> Unit,
) {
    val categoryDialogState = rememberMaterialDialogState()
    LaunchedEffect(Unit) {
        chooseCategoriesFlowHolder.item.collect {
            categoryDialogState.show()
        }
    }
    val readerLauncher = rememberReaderLauncher()
    readerLauncher.Reader()

    BackHandler(inActionMode) {
        clearSelection()
    }

    Scaffold(
        modifier = Modifier
            .onKeyEvent {
                if (inActionMode && it.type == KeyEventType.KeyUp && it.key == Key.Escape) {
                    clearSelection()
                    true
                } else {
                    false
                }
            }
            .windowInsetsPadding(
                WindowInsets.statusBars.add(
                    WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal),
                ),
            ),
        topBar = {
            val navigator = LocalNavigator.current
            Toolbar(
                if (inActionMode) selectedItems.size.toString() else stringResource(MR.strings.location_manga),
                actions = {
                    if (inActionMode) {
                        getActionModeActionItems(
                            selectAll = selectAll,
                            invertSelection = invertSelection,
                        )
                    } else {
                        val uriHandler = LocalUriHandler.current
                        getActionItems(
                            refreshManga = refreshManga,
                            refreshMangaEnabled = !isLoading,
                            categoryItemVisible = categoriesExist && manga?.inLibrary == true,
                            setCategories = setCategories,
                            inLibrary = manga?.inLibrary == true,
                            toggleFavorite = toggleFavorite,
                            favoritesButtonEnabled = manga != null,
                            openInBrowserEnabled = manga?.realUrl != null,
                            openInBrowser = {
                                manga?.realUrl?.let { uriHandler.openUri(it) }
                            },
                            downloadNext = downloadNext,
                            downloadUnread = downloadUnread,
                            downloadAll = downloadAll,
                        )
                    }
                },
                onClose = {
                    if (inActionMode) {
                        clearSelection()
                    } else {
                        navigator?.pop()
                    }
                },
                closeIcon = if (inActionMode) Icons.Rounded.Close else ToolbarDefault,
            )
        },
        bottomBar = {
            BottomActionMenu(
                visible = inActionMode,
                items = getBottomActionItems(
                    selectedItems = selectedItems,
                    markRead = { markRead(null) },
                    markUnread = { markUnread(null) },
                    bookmarkChapter = { bookmarkChapter(null) },
                    unBookmarkChapter = { unBookmarkChapter(null) },
                    markPreviousAsRead = markPreviousRead,
                    deleteChapter = { deleteDownload(null) },
                    downloadChapters = downloadChapters,
                ),
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            manga.let { manga ->
                if (manga != null) {
                    Box {
                        val state = rememberLazyListState()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = state,
                            contentPadding = WindowInsets.bottomNav.add(
                                WindowInsets.navigationBars.only(
                                    WindowInsetsSides.Bottom,
                                ),
                            ).asPaddingValues(),
                        ) {
                            item {
                                MangaItem(manga)
                            }
                            if (chapters.isNotEmpty()) {
                                items(chapters) { chapter ->
                                    ChapterItem(
                                        chapter,
                                        dateTimeFormatter,
                                        onClick = if (inActionMode) {
                                            { if (chapter.isSelected.value) onUnselectChapter(chapter.chapter.id) else onSelectChapter(chapter.chapter.id) }
                                        } else {
                                            { readerLauncher.launch(it, manga.id) }
                                        },
                                        markRead = markRead,
                                        markUnread = markUnread,
                                        bookmarkChapter = bookmarkChapter,
                                        unBookmarkChapter = unBookmarkChapter,
                                        markPreviousAsRead = markPreviousRead,
                                        onClickDownload = downloadChapter,
                                        onClickDeleteChapter = deleteDownload,
                                        onClickStopDownload = stopDownloadingChapter,
                                        onSelectChapter = onSelectChapter,
                                        onUnselectChapter = onUnselectChapter,
                                    )
                                }
                            } else if (!isLoading) {
                                item {
                                    ErrorScreen(
                                        stringResource(MR.strings.no_chapters_found),
                                        Modifier.height(400.dp).fillMaxWidth(),
                                        retry = loadChapters,
                                    )
                                }
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .scrollbarPadding()
                                .windowInsetsPadding(
                                    WindowInsets.bottomNav.add(
                                        WindowInsets.navigationBars.only(
                                            WindowInsetsSides.Bottom,
                                        ),
                                    ),
                                ),
                            adapter = rememberScrollbarAdapter(state),
                        )
                    }
                } else if (!isLoading) {
                    ErrorScreen(stringResource(MR.strings.failed_manga_fetch), retry = loadManga)
                }
            }
            if (isLoading) {
                LoadingScreen()
            }
        }
    }
    CategorySelectDialog(categoryDialogState, availableCategories, mangaCategories, addFavorite)
}

@Composable
@Stable
private fun getActionItems(
    refreshManga: () -> Unit,
    refreshMangaEnabled: Boolean,
    categoryItemVisible: Boolean,
    setCategories: () -> Unit,
    inLibrary: Boolean,
    toggleFavorite: () -> Unit,
    favoritesButtonEnabled: Boolean,
    openInBrowserEnabled: Boolean,
    openInBrowser: () -> Unit,
    downloadNext: (Int) -> Unit,
    downloadUnread: () -> Unit,
    downloadAll: () -> Unit,
): ImmutableList<Action> {
    return listOfNotNull(
        ActionItem(
            name = stringResource(MR.strings.action_refresh_manga),
            icon = Icons.Rounded.Refresh,
            doAction = refreshManga,
            enabled = refreshMangaEnabled,
        ),
        if (categoryItemVisible) {
            ActionItem(
                name = stringResource(MR.strings.edit_categories),
                icon = Icons.Rounded.Label,
                doAction = setCategories,
            )
        } else {
            null
        },
        ActionItem(
            name = stringResource(if (inLibrary) MR.strings.action_remove_favorite else MR.strings.action_favorite),
            icon = if (inLibrary) {
                Icons.Rounded.Favorite
            } else {
                Icons.Rounded.FavoriteBorder
            },
            doAction = toggleFavorite,
            enabled = favoritesButtonEnabled,
        ),
        ActionGroup(
            name = stringResource(MR.strings.action_download),
            icon = Icons.Rounded.Download,
            actions = listOf(
                ActionItem(
                    name = stringResource(MR.strings.download_1),
                    doAction = { downloadNext(1) },
                ),
                ActionItem(
                    name = stringResource(MR.strings.download_5),
                    doAction = { downloadNext(5) },
                ),
                ActionItem(
                    name = stringResource(MR.strings.download_10),
                    doAction = { downloadNext(10) },
                ),
                ActionItem(
                    name = stringResource(MR.strings.download_unread),
                    doAction = downloadUnread,
                ),
                ActionItem(
                    name = stringResource(MR.strings.download_all),
                    doAction = downloadAll,
                ),
            ).toImmutableList(),
        ),
        ActionItem(
            name = stringResource(MR.strings.action_open_in_browser),
            icon = Icons.Rounded.Public,
            enabled = openInBrowserEnabled,
            doAction = openInBrowser,
        ),
    ).toImmutableList()
}

@Composable
@Stable
private fun getActionModeActionItems(
    selectAll: () -> Unit,
    invertSelection: () -> Unit,
): ImmutableList<ActionItem> {
    return listOf(
        ActionItem(
            name = stringResource(MR.strings.action_select_all),
            icon = Icons.Rounded.SelectAll,
            doAction = selectAll,
        ),
        ActionItem(
            name = stringResource(MR.strings.action_select_inverse),
            icon = Icons.Rounded.FlipToBack,
            doAction = invertSelection,
        ),
    ).toImmutableList()
}

@Composable
@Stable
private fun getBottomActionItems(
    selectedItems: ImmutableList<ChapterDownloadItem>,
    markRead: () -> Unit,
    markUnread: () -> Unit,
    bookmarkChapter: () -> Unit,
    unBookmarkChapter: () -> Unit,
    markPreviousAsRead: (Int) -> Unit,
    deleteChapter: () -> Unit,
    downloadChapters: () -> Unit,
): ImmutableList<BottomActionItem> {
    return listOfNotNull(
        BottomActionItem(
            name = stringResource(MR.strings.action_bookmark),
            icon = Icons.Rounded.BookmarkAdd,
            onClick = bookmarkChapter,
        ).takeIf { selectedItems.fastAny { !it.chapter.bookmarked } },
        BottomActionItem(
            name = stringResource(MR.strings.action_remove_bookmark),
            icon = Icons.Rounded.BookmarkRemove,
            onClick = unBookmarkChapter,
        ).takeIf { selectedItems.fastAny { it.chapter.bookmarked } },
        BottomActionItem(
            name = stringResource(MR.strings.action_mark_as_read),
            icon = Icons.Rounded.DoneAll,
            onClick = markRead,
        ).takeIf { selectedItems.fastAny { !it.chapter.read } },
        BottomActionItem(
            name = stringResource(MR.strings.action_mark_as_unread),
            icon = Icons.Rounded.RemoveDone,
            onClick = markUnread,
        ).takeIf { selectedItems.fastAny { it.chapter.read } },
        BottomActionItem(
            name = stringResource(MR.strings.action_mark_previous_read),
            icon = JuiAssets.DonePrev,
            onClick = { markPreviousAsRead(selectedItems.first().chapter.index) },
        ).takeIf { selectedItems.size == 1 },
        BottomActionItem(
            name = stringResource(MR.strings.action_download),
            icon = Icons.Rounded.Download,
            onClick = downloadChapters,
        ).takeIf { selectedItems.fastAny { it.downloadState.value == ChapterDownloadState.NotDownloaded } },
        BottomActionItem(
            name = stringResource(MR.strings.action_delete),
            icon = Icons.Rounded.Delete,
            onClick = deleteChapter,
        ).takeIf { selectedItems.fastAny { it.downloadState.value == ChapterDownloadState.Downloaded } },
    ).toImmutableList()
}
