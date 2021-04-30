/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import ca.gosyer.data.models.Chapter
import ca.gosyer.ui.base.components.ErrorScreen
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.Pager
import ca.gosyer.ui.base.components.PagerState
import ca.gosyer.ui.base.components.mangaAspectRatio
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.util.compose.ThemedWindow

fun openReaderMenu(chapterIndex: Int, mangaId: Long) {
    ThemedWindow("TachideskJUI - Reader") {
        ReaderMenu(chapterIndex, mangaId)
    }
}

@Composable
fun ReaderMenu(chapterIndex: Int, mangaId: Long) {
    val vm = viewModel<ReaderMenuViewModel> {
        ReaderMenuViewModel.Params(chapterIndex, mangaId)
    }
    val isLoading by vm.isLoading.collectAsState()
    val chapter by vm.chapter.collectAsState()
    val pages by vm.pages.collectAsState()
    val continuous by vm.readerModeSettings.continuous.collectAsState()
    val direction by vm.readerModeSettings.direction.collectAsState()
    val padding by vm.readerModeSettings.padding.collectAsState()
    val currentPage by vm.currentPage.collectAsState()

    Surface {
        if (!isLoading && chapter != null) {
            chapter?.let { chapter ->
                val pageModifier = Modifier.fillMaxWidth().aspectRatio(mangaAspectRatio)
                if (pages.isNotEmpty()) {
                    if (continuous) {
                        ContinuesReader(vm, pages, pageModifier)
                    } else {
                        PagerReader(vm, chapter, currentPage, pages, pageModifier)
                    }
                } else {
                    ErrorScreen("No pages found")
                }
            }
        } else {
            LoadingScreen(isLoading)
        }
    }
}

@Composable
fun ReaderImage(
    imageIndex: Int,
    drawable: ImageBitmap?,
    loading: Boolean,
    error: String?,
    imageModifier: Modifier = Modifier.fillMaxSize(),
    loadingModifier: Modifier = imageModifier,
    contentScale: ContentScale = ContentScale.Fit,
    retry: (Int) -> Unit
) {
    if (drawable != null) {
        Image(
            drawable,
            modifier = imageModifier,
            contentDescription = null,
            contentScale = contentScale
        )
    } else {
        LoadingScreen(loading, loadingModifier, error) { retry(imageIndex) }
    }
}

@Composable
fun PagerReader(readerVM: ReaderMenuViewModel, chapter: Chapter, currentPage: Int, pages: List<ReaderImage>, pageModifier: Modifier) {
    val state = remember(chapter.pageCount!!, currentPage) {
        PagerState(
            currentPage = currentPage,
            minPage = 1,
            maxPage = chapter.pageCount - 1
        )
    }
    LaunchedEffect(state.currentPage) {
        if (state.currentPage != currentPage) {
            readerVM.progress(state.currentPage)
        }
    }
    Pager(state) {
        val image = pages[page - 1]
        ReaderImage(
            image.index,
            image.bitmap.collectAsState().value,
            image.loading.collectAsState().value,
            image.error.collectAsState().value,
            loadingModifier = pageModifier,
            retry = readerVM::retry
        )
    }
}

@Composable
fun ContinuesReader(readerVM: ReaderMenuViewModel, pages: List<ReaderImage>, pageModifier: Modifier) {
    LazyColumn {
        items(pages) { image ->
            LaunchedEffect(image.index) {
                readerVM.progress(image.index)
            }
            ReaderImage(
                image.index,
                image.bitmap.collectAsState().value,
                image.loading.collectAsState().value,
                image.error.collectAsState().value,
                loadingModifier = pageModifier,
                retry = readerVM::retry
            )
        }
    }
}
