/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Manga
import ca.gosyer.data.models.Source
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.MangaGridItem
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.util.compose.persistentLazyListState
import com.github.zsoltk.compose.savedinstancestate.Bundle

@Composable
fun SourceScreen(
    bundle: Bundle,
    source: Source,
    onMangaClick: (Long) -> Unit,
    enableSearch: (Boolean) -> Unit,
    setSearch: ((String?) -> Unit) -> Unit
) {
    val vm = viewModel<SourceScreenViewModel>(source.id) {
        SourceScreenViewModel.Params(source, bundle)
    }
    val mangas by vm.mangas.collectAsState()
    val hasNextPage by vm.hasNextPage.collectAsState()
    val loading by vm.loading.collectAsState()
    val isLatest by vm.isLatest.collectAsState()
    val serverUrl by vm.serverUrl.collectAsState()

    LaunchedEffect(Unit) {
        setSearch(vm::search)
    }

    DisposableEffect(isLatest) {
        enableSearch(!isLatest)

        onDispose {
            enableSearch(false)
        }
    }

    MangaTable(
        bundle,
        mangas,
        loading,
        hasNextPage,
        source.supportsLatest,
        isLatest,
        serverUrl,
        onLoadNextPage = vm::loadNextPage,
        onMangaClick = onMangaClick,
        onClickMode = vm::setMode
    )
}

@Composable
private fun MangaTable(
    bundle: Bundle,
    mangas: List<Manga>,
    isLoading: Boolean = false,
    hasNextPage: Boolean = false,
    supportsLatest: Boolean,
    isLatest: Boolean,
    serverUrl: String,
    onLoadNextPage: () -> Unit,
    onMangaClick: (Long) -> Unit,
    onClickMode: (Boolean) -> Unit
) {
    if (isLoading || mangas.isEmpty()) {
        LoadingScreen(isLoading)
    } else {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (supportsLatest) {
                    Button(
                        onClick = { onClickMode(!isLatest) },
                        enabled = !isLoading,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(text = stringResource(if (isLatest) "move_to_browse" else "move_to_latest"))
                    }
                }
            }

            val persistentState = persistentLazyListState(bundle)
            LazyVerticalGrid(GridCells.Adaptive(160.dp), state = persistentState) {
                itemsIndexed(mangas) { index, manga ->
                    if (hasNextPage && index == mangas.lastIndex) {
                        LaunchedEffect(Unit) { onLoadNextPage() }
                    }
                    MangaGridItem(
                        title = manga.title,
                        cover = manga.cover(serverUrl),
                        onClick = {
                            onMangaClick(manga.id)
                        }
                    )
                }
            }
        }
    }
}
