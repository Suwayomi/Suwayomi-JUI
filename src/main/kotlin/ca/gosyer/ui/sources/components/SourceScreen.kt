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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Manga
import ca.gosyer.data.models.Source
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.MangaGridItem
import ca.gosyer.ui.base.vm.viewModel
import com.github.zsoltk.compose.savedinstancestate.Bundle
import com.github.zsoltk.compose.savedinstancestate.LocalSavedInstanceState

@Composable
fun SourceScreen(
    source: Source,
    onMangaClick: (Long) -> Unit
) {
    val upstream = LocalSavedInstanceState.current

    val vm = viewModel<SourceScreenViewModel>()
    val bundle = remember(source.id) {
        upstream.getBundle(source.id.toString())
            ?: Bundle().also { upstream.putBundle(source.id.toString(), it) }
    }
    remember(source.id) {
        vm.init(source, bundle)
    }
    val mangas by vm.mangas.collectAsState()
    val hasNextPage by vm.hasNextPage.collectAsState()
    val loading by vm.loading.collectAsState()
    val isLatest by vm.isLatest.collectAsState()
    val serverUrl by vm.serverUrl.collectAsState()

    MangaTable(
        mangas,
        loading,
        hasNextPage,
        isLatest,
        serverUrl,
        onLoadNextPage = vm::loadNextPage,
        onMangaClick = onMangaClick,
        onClickMode = vm::setMode
    )
}

@Composable
private fun MangaTable(
    mangas: List<Manga>,
    isLoading: Boolean = false,
    hasNextPage: Boolean = false,
    isLatest: Boolean,
    serverUrl: String,
    onLoadNextPage: () -> Unit,
    onMangaClick: (Long) -> Unit,
    onClickMode: (Boolean) -> Unit
) {
    if (mangas.isEmpty()) {
        LoadingScreen(isLoading)
    } else {
        Column {
            // TODO: this should happen automatically on scroll
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onLoadNextPage,
                    enabled = hasNextPage && !isLoading,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(text = if (isLoading) "Loading..." else "Load next page")
                }
                Button(
                    onClick = { onClickMode(!isLatest) },
                    enabled = !isLoading,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(text = if (isLatest) "Latest" else "Browse")
                }
            }

            LazyVerticalGrid(GridCells.Adaptive(160.dp)) {
                items(mangas) { manga ->
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
