/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.interactor

import ca.gosyer.jui.domain.ServerListeners
import ca.gosyer.jui.domain.manga.interactor.GetManga
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.source.model.MangaPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class SourcePager @Inject constructor(
    private val getManga: GetManga,
    private val serverListeners: ServerListeners,
    private val fetcher: suspend (page: Int) -> MangaPage?,
) : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {
    private val sourceMutex = Mutex()

    private val _sourceManga = MutableStateFlow<List<Manga>>(emptyList())

    private val mangaIds = _sourceManga.map { mangas -> mangas.map { it.id } }
        .stateIn(this, SharingStarted.Eagerly, emptyList())

    private val changedManga = serverListeners.mangaListener.runningFold(emptyMap<Long, Manga>()) { manga, updatedMangaIds ->
        coroutineScope {
            manga + updatedMangaIds.filter { it in mangaIds.value }.map {
                async {
                    getManga.await(it)
                }
            }.awaitAll().filterNotNull().associateBy { it.id }
        }
    }.stateIn(this, SharingStarted.Eagerly, emptyMap())

    val mangas = combine(_sourceManga, changedManga) { sourceManga, changedManga ->
        sourceManga.map { changedManga[it.id] ?: it }
    }.stateIn(this, SharingStarted.Eagerly, emptyList())

    private val _pageNum = MutableStateFlow(0)
    val pageNum = _pageNum.asStateFlow()

    private val _hasNextPage = MutableStateFlow(true)
    val hasNextPage = _hasNextPage.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    fun loadNextPage() {
        launch {
            if (hasNextPage.value && sourceMutex.tryLock()) {
                _pageNum.value++
                val page = fetcher(_pageNum.value)
                if (page != null) {
                    _sourceManga.value = _sourceManga.value + page.mangaList
                    _hasNextPage.value = page.hasNextPage
                } else {
                    _pageNum.value--
                }
                sourceMutex.unlock()
            }
            _loading.value = false
        }
    }

    companion object {
        private val log = logging()
    }
}