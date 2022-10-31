/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.browse.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ca.gosyer.jui.uicore.insets.navigationBars
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SourceMangaList(
    mangas: ImmutableList<Manga>,
    onClickManga: (Long) -> Unit,
    hasNextPage: Boolean = false,
    onLoadNextPage: () -> Unit
) {
    Box {
        val state = rememberLazyListState()
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.bottomNav.add(
                WindowInsets.navigationBars.only(
                    WindowInsetsSides.Bottom
                )
            ).asPaddingValues()
        ) {
            itemsIndexed(mangas) { index, manga ->
                if (hasNextPage && index == mangas.lastIndex) {
                    LaunchedEffect(Unit) { onLoadNextPage() }
                }
                MangaListItem(
                    modifier = Modifier.clickable(
                        onClick = { onClickManga(manga.id) }
                    ),
                    manga = manga,
                    inLibrary = manga.inLibrary
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
                            WindowInsetsSides.Bottom
                        )
                    )
                )
        )
    }
}

@Composable
private fun MangaListItem(
    modifier: Modifier,
    manga: Manga,
    inLibrary: Boolean
) {
    MangaListItem(
        modifier = modifier then Modifier
            .requiredHeight(56.dp)
            .padding(horizontal = 16.dp)
    ) {
        MangaListItemImage(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium),
            data = manga,
            contentDescription = manga.title
        )
        MangaListItemTitle(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            text = manga.title
        )
        SourceMangaBadges(inLibrary)
    }
}
