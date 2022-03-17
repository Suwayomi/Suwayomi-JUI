/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Manga
import ca.gosyer.uicore.components.MangaListItem
import ca.gosyer.uicore.components.MangaListItemImage
import ca.gosyer.uicore.components.MangaListItemTitle
import ca.gosyer.uicore.components.VerticalScrollbar
import ca.gosyer.uicore.components.rememberScrollbarAdapter
import io.kamel.image.lazyPainterResource

@Composable
fun LibraryMangaList(
    library: List<Manga>,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit
) {
    Box {
        val state = rememberLazyListState()
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize()
        ) {
            items(library) { manga ->
                LibraryMangaListItem(
                    modifier = Modifier.libraryMangaModifier(
                        { onClickManga(manga.id) },
                        { onRemoveMangaClicked(manga.id) }
                    ),
                    manga = manga,
                    unread = manga.unreadCount,
                    downloaded = manga.downloadCount,
                )
            }
        }
        VerticalScrollbar(
            rememberScrollbarAdapter(state),
            Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun LibraryMangaListItem(
    modifier: Modifier,
    manga: Manga,
    unread: Int?,
    downloaded: Int?,
) {
    val cover = lazyPainterResource(manga, filterQuality = FilterQuality.Medium)
    MangaListItem(
        modifier = modifier then Modifier
            .requiredHeight(56.dp)
            .padding(horizontal = 16.dp),
    ) {
        MangaListItemImage(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium),
            cover = cover,
            contentDescription = manga.title
        )
        MangaListItemTitle(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            text = manga.title,
        )
        LibraryMangaBadges(unread, downloaded)
    }
}
