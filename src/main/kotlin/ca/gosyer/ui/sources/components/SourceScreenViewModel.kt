/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import ca.gosyer.data.models.Manga
import ca.gosyer.data.models.MangaPage
import ca.gosyer.data.models.Source
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SourceScreenViewModel @Inject constructor(
    private val sourceHandler: SourceInteractionHandler,
    serverPreferences: ServerPreferences
): ViewModel() {
    private lateinit var source: Source

    val serverUrl = serverPreferences.server().stateIn(scope)

    private val _mangas = MutableStateFlow(emptyList<Manga>())
    val mangas = _mangas.asStateFlow()

    private val _hasNextPage = MutableStateFlow(false)
    val hasNextPage = _hasNextPage.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _isLatest = MutableStateFlow(true)
    val isLatest = _isLatest.asStateFlow()

    private val _pageNum = MutableStateFlow(1)
    val pageNum = _pageNum.asStateFlow()

    fun init(source: Source) {
        this.source = source
        scope.launch {
            _loading.value = true
            _mangas.value = emptyList()
            _hasNextPage.value = false
            _pageNum.value = 1
            _isLatest.value = source.supportsLatest
            val page = getPage()
            _mangas.value += page.mangaList
            _hasNextPage.value = page.hasNextPage
            _loading.value = false
        }
    }

    fun loadNextPage() {
        scope.launch {
            _hasNextPage.value = false
            _pageNum.value++
            val page = getPage()
            _mangas.value += page.mangaList
            _hasNextPage.value = page.hasNextPage
            _loading.value = false
        }
    }

    fun setMode(toLatest: Boolean) {
        if (isLatest.value != toLatest){
            _isLatest.value = toLatest
            init(source)
        }
    }

    private suspend fun getPage(): MangaPage {
        return if (isLatest.value) {
            sourceHandler.getLatestManga(source, pageNum.value)
        } else {
            sourceHandler.getPopularManga(source, pageNum.value)
        }
    }
}