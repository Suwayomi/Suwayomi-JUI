/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader

import androidx.compose.ui.graphics.ImageBitmap
import ca.gosyer.data.models.Chapter
import ca.gosyer.data.reader.ReaderModeWatch
import ca.gosyer.data.reader.ReaderPreferences
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

class ReaderMenuViewModel @Inject constructor(
    params: Params,
    readerPreferences: ReaderPreferences,
    chapterHandler: ChapterInteractionHandler
) : ViewModel() {
    private val _chapter = MutableStateFlow<Chapter?>(null)
    val chapter = _chapter.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _pages = MutableStateFlow(emptyList<ReaderImage>())
    val pages = _pages.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    val readerModeSettings = ReaderModeWatch(readerPreferences, scope)

    init {
        scope.launch(Dispatchers.Default) {
            val chapter: Chapter
            _chapter.value = chapterHandler.getChapter(params.mangaId, params.chapterIndex).also { chapter = it }
            val pageRange = 1..(chapter.pageCount ?: 1)
            _pages.value = pageRange.map {
                ReaderImage(
                    it,
                    MutableStateFlow(null),
                    MutableStateFlow(true),
                    MutableStateFlow(null)
                )
            }

            _isLoading.value = false

            val semaphore = Semaphore(3)
            pageRange.map {
                async {
                    semaphore.withPermit {
                        val page = _pages.value[it - 1]
                        try {
                            page.bitmap.value = chapterHandler.getPage(chapter, it)
                            page.loading.value = false
                            page.error.value = null
                        } catch (e: Exception) {
                            page.bitmap.value = null
                            page.loading.value = false
                            page.error.value = e.message
                        }
                    }
                }
            }.awaitAll()
        }
    }

    fun progress(index: Int) {
        _currentPage.value = index
    }

    fun retry(index: Int) {
    }

    data class Params(val chapterIndex: Int, val mangaId: Long)
}

data class ReaderImage(
    val index: Int,
    val bitmap: MutableStateFlow<ImageBitmap?>,
    val loading: MutableStateFlow<Boolean>,
    val error: MutableStateFlow<String?>
)
