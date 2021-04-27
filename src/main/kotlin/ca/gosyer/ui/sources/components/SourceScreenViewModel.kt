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
import ca.gosyer.data.server.interactions.MangaInteractionHandler
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.compose.getJsonObjectArray
import ca.gosyer.util.compose.putJsonObjectArray
import com.github.zsoltk.compose.savedinstancestate.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class SourceScreenViewModel @Inject constructor(
    private val sourceHandler: SourceInteractionHandler,
    private val mangaHandler: MangaInteractionHandler,
    serverPreferences: ServerPreferences
) : ViewModel() {
    private lateinit var source: Source
    private lateinit var bundle: Bundle

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

    init {
        _mangas.drop(1)
            .onEach { manga ->
                bundle.putJsonObjectArray(MANGAS_KEY, manga)
            }
            .launchIn(scope)
        _hasNextPage.drop(1)
            .onEach {
                bundle.putBoolean(NEXT_PAGE_KEY, it)
            }
            .launchIn(scope)
        _pageNum.drop(1)
            .onEach {
                bundle.putInt(PAGE_NUM_KEY, it)
            }
            .launchIn(scope)
    }

    fun init(source: Source, bundle: Bundle) {
        this.source = source
        this.bundle = bundle
        scope.launch {
            _loading.value = true
            _mangas.value = emptyList()
            _hasNextPage.value = false
            _pageNum.value = bundle.getInt(PAGE_NUM_KEY, 1)
            _isLatest.value = bundle.getBoolean(IS_LATEST_KEY, source.supportsLatest)
            val page = bundle.getJsonObjectArray<Manga>(MANGAS_KEY)
                ?.let {
                    MangaPage(it.filterNotNull(), bundle.getBoolean(NEXT_PAGE_KEY, true))
                }
                ?: getPage()
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
        if (isLatest.value != toLatest) {
            _isLatest.value = toLatest
            bundle.remove(MANGAS_KEY)
            bundle.remove(NEXT_PAGE_KEY)
            bundle.remove(PAGE_NUM_KEY)
            bundle.remove(IS_LATEST_KEY)
            init(source, bundle)
        }
    }

    private suspend fun getPage(): MangaPage {
        return if (isLatest.value) {
            sourceHandler.getLatestManga(source, pageNum.value)
        } else {
            sourceHandler.getPopularManga(source, pageNum.value)
        }
    }

    companion object {
        const val MANGAS_KEY = "mangas"
        const val NEXT_PAGE_KEY = "next_page"
        const val PAGE_NUM_KEY = "next_page"
        const val IS_LATEST_KEY = "is_latest"
    }
}
