/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Manga
import ca.gosyer.uicore.components.VerticalScrollbar
import ca.gosyer.uicore.components.mangaAspectRatio
import ca.gosyer.uicore.components.rememberScrollbarAdapter
import ca.gosyer.uicore.image.KamelImage
import io.kamel.image.lazyPainterResource

@Composable
fun LibraryMangaCoverOnlyGrid(
    library: List<Manga>,
    gridColumns: Int,
    gridSize: Int,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit
) {
    Box {
        val state = rememberLazyListState()
        val cells = if (gridColumns < 1) {
            GridCells.Adaptive(gridSize.dp)
        } else {
            GridCells.Fixed(gridColumns)
        }
        LazyVerticalGrid(
            cells = cells,
            state = state,
            modifier = Modifier.fillMaxSize().padding(4.dp)
        ) {
            items(library) { manga ->
                LibraryMangaCoverOnlyGridItem(
                    modifier = Modifier.libraryMangaModifier(
                        { onClickManga(manga.id) },
                        { onRemoveMangaClicked(manga.id) }
                    ),
                    manga = manga,
                    unread = manga.unreadCount,
                    downloaded = manga.downloadCount
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
private fun LibraryMangaCoverOnlyGridItem(
    modifier: Modifier,
    manga: Manga,
    unread: Int?,
    downloaded: Int?
) {
    val cover = lazyPainterResource(manga, filterQuality = FilterQuality.Medium)

    Box(
        modifier = Modifier.padding(4.dp)
            .fillMaxWidth()
            .aspectRatio(mangaAspectRatio)
            .clip(MaterialTheme.shapes.medium) then modifier
    ) {
        KamelImage(
            cover,
            contentDescription = manga.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        LibraryMangaBadges(
            unread = unread,
            downloaded = downloaded,
            modifier = Modifier.padding(4.dp)
        )
    }
}
