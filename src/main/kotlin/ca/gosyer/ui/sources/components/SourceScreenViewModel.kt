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
import ca.gosyer.util.compose.getJsonObjectArray
import ca.gosyer.util.compose.putJsonObjectArray
import ca.gosyer.util.lang.seconds
import com.github.zsoltk.compose.savedinstancestate.Bundle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class SourceScreenViewModel @Inject constructor(
    private val sourceHandler: SourceInteractionHandler,
    serverPreferences: ServerPreferences
) : ViewModel() {
    private lateinit var source: Source
    private var bundle: Bundle? = null

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
                bundle?.putJsonObjectArray(MANGAS_KEY, manga)
            }
            .launchIn(scope)
        _hasNextPage.drop(1)
            .onEach {
                bundle?.putBoolean(NEXT_PAGE_KEY, it)
            }
            .launchIn(scope)
        _pageNum.drop(1)
            .onEach {
                bundle?.putInt(PAGE_NUM_KEY, it)
            }
            .launchIn(scope)
    }

    fun removeOldSource() {
        bundle = null
        _loading.value = true
        _isLatest.value = true
        _mangas.value = emptyList()
        _hasNextPage.value = false
        _pageNum.value = 1
    }

    fun init(source: Source, bundle: Bundle, toLatest: Boolean = source.supportsLatest) {
        scope.launch {
            // Delay because there seems to be a data race between compose and the assignments,
            // it will continue to show the old sources items unless its delayed
            delay(0.5.seconds)
            this@SourceScreenViewModel.source = source
            this@SourceScreenViewModel.bundle = bundle
            _pageNum.value = bundle.getInt(PAGE_NUM_KEY, 1)
            _isLatest.value = bundle.getBoolean(IS_LATEST_KEY, toLatest)
            val page = bundle.getJsonObjectArray<Manga>(MANGAS_KEY)
                ?.let {
                    MangaPage(it.filterNotNull(), bundle.getBoolean(NEXT_PAGE_KEY, true))
                }
                ?: getPage()
            _mangas.value = page.mangaList
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
        val bundle = bundle ?: return
        if (isLatest.value != toLatest) {
            bundle.remove(MANGAS_KEY)
            bundle.remove(NEXT_PAGE_KEY)
            bundle.remove(PAGE_NUM_KEY)
            bundle.remove(IS_LATEST_KEY)
            init(source, bundle, toLatest)
        }
    }

    private suspend fun getPage(): MangaPage {
        return if (isLatest.value) {
            sourceHandler.getLatestManga(source, pageNum.value)
        } else {
            sourceHandler.getPopularManga(source, pageNum.value)
        }
    }

    private companion object {
        const val MANGAS_KEY = "mangas"
        const val NEXT_PAGE_KEY = "next_page"
        const val PAGE_NUM_KEY = "next_page"
        const val IS_LATEST_KEY = "is_latest"
    }
}
