/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import ca.gosyer.backend.models.Manga
import ca.gosyer.backend.models.MangaPage
import ca.gosyer.backend.models.Source
import ca.gosyer.backend.network.interactions.SourceInteractionHandler
import ca.gosyer.backend.preferences.PreferenceHelper
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.system.asStateFlow
import ca.gosyer.util.system.inject
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SourceScreenViewModel: ViewModel() {
    private lateinit var source: Source
    private val preferences: PreferenceHelper by inject()
    private val httpClient: HttpClient by inject()

    val serverUrl = preferences.serverUrl.asFLow()
        .asStateFlow(preferences.serverUrl.get(),scope, true)

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
            SourceInteractionHandler(httpClient).getLatestManga(source, pageNum.value)
        } else {
            SourceInteractionHandler(httpClient).getPopularManga(source, pageNum.value)
        }
    }
}