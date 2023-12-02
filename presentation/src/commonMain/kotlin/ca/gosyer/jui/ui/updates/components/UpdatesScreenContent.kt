/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.updates.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FlipToBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import ca.gosyer.jui.domain.base.WebsocketService
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadState
import ca.gosyer.jui.ui.base.navigation.ActionItem
import ca.gosyer.jui.ui.base.navigation.BackHandler
import ca.gosyer.jui.ui.base.navigation.OverflowMode
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.navigation.ToolbarDefault
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.ui.updates.UpdatesUI
import ca.gosyer.jui.uicore.components.BottomActionItem
import ca.gosyer.jui.uicore.components.BottomActionMenu
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.resources.stringResource
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun UpdatesScreenContent(
    isLoading: Boolean,
    updates: ImmutableList<UpdatesUI>,
    inActionMode: Boolean,
    selectedItems: ImmutableList<ChapterDownloadItem>,
    loadNextPage: () -> Unit,
    openChapter: (index: Int, mangaId: Long) -> Unit,
    openManga: (Long) -> Unit,
    markRead: (Long?) -> Unit,
    markUnread: (Long?) -> Unit,
    bookmarkChapter: (Long?) -> Unit,
    unBookmarkChapter: (Long?) -> Unit,
    downloadChapter: (Chapter?) -> Unit,
    deleteDownloadedChapter: (Chapter?) -> Unit,
    stopDownloadingChapter: (Chapter) -> Unit,
    onSelectChapter: (Long) -> Unit,
    onUnselectChapter: (Long) -> Unit,
    selectAll: () -> Unit,
    invertSelection: () -> Unit,
    clearSelection: () -> Unit,
    onUpdateLibrary: () -> Unit,
    updateWebsocketStatus: WebsocketService.Status,
    restartLibraryUpdates: () -> Unit,
) {
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
                if (inActionMode) selectedItems.size.toString() else stringResource(MR.strings.location_updates),
                actions = {
                    if (inActionMode) {
                        getActionModeActionItems(
                            selectAll = selectAll,
                            invertSelection = invertSelection,
                        )
                    } else {
                        getActionItems(
                            onUpdateLibrary,
                            updateWebsocketStatus,
                            restartLibraryUpdates, // todo set null if wide screen
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
                    deleteChapter = { deleteDownloadedChapter(null) },
                    downloadChapters = { downloadChapter(null) },
                ),
            )
        },
    ) {
        if (isLoading || updates.isEmpty()) {
            LoadingScreen(isLoading)
        } else {
            Box(Modifier.padding(it)) {
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
                    itemsIndexed(updates) { index, item ->
                        LaunchedEffect(Unit) {
                            if (index == updates.lastIndex) {
                                loadNextPage()
                            }
                        }
                        when (item) {
                            is UpdatesUI.Header -> Text(
                                text = item.date,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium,
                            )
                            is UpdatesUI.Item -> {
                                val manga = item.chapterDownloadItem.manga!!
                                val chapter = item.chapterDownloadItem.chapter
                                UpdatesItem(
                                    chapterDownloadItem = item.chapterDownloadItem,
                                    onClickItem = if (inActionMode) {
                                        {
                                            if (item.chapterDownloadItem.isSelected.value) onUnselectChapter(item.chapterDownloadItem.chapter.id) else onSelectChapter(item.chapterDownloadItem.chapter.id)
                                        }
                                    } else {
                                        { openChapter(chapter.index, manga.id) }
                                    },
                                    markRead = markRead,
                                    markUnread = markUnread,
                                    bookmarkChapter = bookmarkChapter,
                                    unBookmarkChapter = unBookmarkChapter,
                                    onSelectChapter = onSelectChapter,
                                    onUnselectChapter = onUnselectChapter,
                                    onClickCover = { openManga(manga.id) },
                                    onClickDownload = downloadChapter,
                                    onClickDeleteDownload = deleteDownloadedChapter,
                                    onClickStopDownload = stopDownloadingChapter,
                                )
                            }
                        }
                    }
                }
                VerticalScrollbar(
                    rememberScrollbarAdapter(state),
                    Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .scrollbarPadding()
                        .windowInsetsPadding(
                            WindowInsets.bottomNav.add(
                                WindowInsets.navigationBars.only(
                                    WindowInsetsSides.Bottom,
                                ),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
@Stable
private fun getActionItems(
    onUpdateLibrary: () -> Unit,
    updateWebsocketStatus: WebsocketService.Status? = null,
    restartLibraryUpdates: (() -> Unit)? = null,
): ImmutableList<ActionItem> {
    return listOfNotNull(
        ActionItem(
            name = stringResource(MR.strings.action_update_library),
            icon = Icons.Rounded.Refresh,
            doAction = onUpdateLibrary,
        ),
        if (updateWebsocketStatus == WebsocketService.Status.STOPPED && restartLibraryUpdates != null) {
            ActionItem(
                name = stringResource(MR.strings.action_restart_library),
                overflowMode = OverflowMode.ALWAYS_OVERFLOW,
                doAction = restartLibraryUpdates,
            )
        } else {
            null
        },
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
