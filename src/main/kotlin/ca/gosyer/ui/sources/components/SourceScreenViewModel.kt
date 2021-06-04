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
import ca.gosyer.util.compose.saveBooleanInBundle
import ca.gosyer.util.compose.saveIntInBundle
import ca.gosyer.util.compose.saveObjectInBundle
import com.github.zsoltk.compose.savedinstancestate.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SourceScreenViewModel(
    private val source: Source,
    private val bundle: Bundle,
    private val sourceHandler: SourceInteractionHandler,
    serverPreferences: ServerPreferences
) : ViewModel() {

    @Inject constructor(
        params: Params,
        sourceHandler: SourceInteractionHandler,
        serverPreferences: ServerPreferences
    ) : this(
        params.source,
        params.bundle,
        sourceHandler,
        serverPreferences
    )
    val serverUrl = serverPreferences.serverUrl().stateIn(scope)

    private val _mangas = saveObjectInBundle(scope, bundle, MANGAS_KEY) { emptyList<Manga>() }
    val mangas = _mangas.asStateFlow()

    private val _hasNextPage = saveBooleanInBundle(scope, bundle, NEXT_PAGE_KEY, false)
    val hasNextPage = _hasNextPage.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _isLatest = saveBooleanInBundle(scope, bundle, IS_LATEST_KEY, false)
    val isLatest = _isLatest.asStateFlow()

    private val _pageNum = saveIntInBundle(scope, bundle, PAGE_NUM_KEY, 1)
    val pageNum = _pageNum.asStateFlow()

    init {
        scope.launch {
            if (bundle[MANGAS_KEY] == null) {
                val (mangas, hasNextPage) = getPage()
                _mangas.value = mangas
                _hasNextPage.value = hasNextPage
            }
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
            bundle.remove(MANGAS_KEY)
            bundle.remove(NEXT_PAGE_KEY)
            bundle.remove(PAGE_NUM_KEY)
            bundle.remove(IS_LATEST_KEY)
            _isLatest.value = toLatest
            // [loadNextPage] increments by 1
            _pageNum.value = 0
            _loading.value = true
            _mangas.value = emptyList()
            loadNextPage()
        }
    }

    private suspend fun getPage(): MangaPage {
        return if (isLatest.value) {
            sourceHandler.getLatestManga(source, pageNum.value)
        } else {
            sourceHandler.getPopularManga(source, pageNum.value)
        }
    }

    data class Params(val source: Source, val bundle: Bundle)

    private companion object {
        const val MANGAS_KEY = "mangas"
        const val NEXT_PAGE_KEY = "next_page"
        const val PAGE_NUM_KEY = "next_page"
        const val IS_LATEST_KEY = "is_latest"
    }
}
