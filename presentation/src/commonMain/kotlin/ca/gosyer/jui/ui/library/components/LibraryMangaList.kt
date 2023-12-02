/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.uicore.components.MangaListItem
import ca.gosyer.jui.uicore.components.MangaListItemImage
import ca.gosyer.jui.uicore.components.MangaListItemTitle
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import kotlinx.collections.immutable.ImmutableList

@Composable
fun LibraryMangaList(
    library: ImmutableList<Manga>,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit,
    showUnread: Boolean,
    showDownloaded: Boolean,
    showLanguage: Boolean,
    showLocal: Boolean,
) {
    Box {
        val state = rememberLazyListState()
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.bottomNav.add(
                WindowInsets.navigationBars.only(
                    WindowInsetsSides.Bottom,
                ),
            ).asPaddingValues(),
        ) {
            items(library) { manga ->
                LibraryMangaListItem(
                    modifier = Modifier.libraryMangaModifier(
                        { onClickManga(manga.id) },
                        { onRemoveMangaClicked(manga.id) },
                    ),
                    manga = manga,
                    showUnread = showUnread,
                    showDownloaded = showDownloaded,
                    showLanguage = showLanguage,
                    showLocal = showLocal,
                )
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

@Composable
private fun LibraryMangaListItem(
    modifier: Modifier,
    manga: Manga,
    showUnread: Boolean,
    showDownloaded: Boolean,
    showLanguage: Boolean,
    showLocal: Boolean,
) {
    MangaListItem(
        modifier = modifier then Modifier
            .requiredHeight(56.dp)
            .padding(horizontal = 16.dp),
    ) {
        MangaListItemImage(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium),
            data = manga,
            contentDescription = manga.title,
        )
        MangaListItemTitle(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            text = manga.title,
        )
        Box(Modifier.width(IntrinsicSize.Min)) {
            LibraryMangaBadges(
                manga = manga,
                showUnread = showUnread,
                showDownloaded = showDownloaded,
                showLanguage = showLanguage,
                showLocal = showLocal,
            )
        }
    }
}
