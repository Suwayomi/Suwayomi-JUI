/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.updates.interactor

import androidx.compose.runtime.Immutable
import ca.gosyer.jui.domain.ServerListeners
import ca.gosyer.jui.domain.chapter.interactor.GetChapter
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.manga.interactor.GetManga
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.updates.model.MangaAndChapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

class UpdatesPager
    @Inject
    constructor(
        private val getRecentUpdates: GetRecentUpdates,
        private val getManga: GetManga,
        private val getChapter: GetChapter,
        private val serverListeners: ServerListeners,
    ) : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {
        private val updatesMutex = Mutex()

        private val fetchedUpdates = MutableSharedFlow<List<MangaAndChapter>>()
        private val foldedUpdates = fetchedUpdates.runningFold(emptyList<Updates>()) { updates, newUpdates ->
            updates.ifEmpty {
                val first = newUpdates.firstOrNull()?.chapter ?: return@runningFold updates
                listOf(
                    Updates.Date(
                        Instant.fromEpochSeconds(first.fetchedAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date,
                    ),
                )
            } + newUpdates.fold(emptyList()) { list, (manga, chapter) ->
                val date = (list.lastOrNull() as? Updates.Update)?.let {
                    val lastUpdateDate = Instant.fromEpochSeconds(it.chapter.fetchedAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                    val chapterDate = Instant.fromEpochSeconds(chapter.fetchedAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                    chapterDate.takeUnless { it == lastUpdateDate }
                }

                if (date == null) {
                    list + Updates.Update(manga, chapter)
                } else {
                    list + Updates.Date(date) + Updates.Update(manga, chapter)
                }
            }
        }.stateIn(this, SharingStarted.Eagerly, emptyList())

        private val mangaIds = foldedUpdates.map { updates ->
            updates.filterIsInstance<Updates.Update>().map { it.manga.id }
        }.stateIn(this, SharingStarted.Eagerly, emptyList())
        private val chapterIds = foldedUpdates.map { updates ->
            updates.filterIsInstance<Updates.Update>().map { Triple(it.manga.id, it.chapter.index, it.chapter.id) }
        }.stateIn(this, SharingStarted.Eagerly, emptyList())

        private val changedManga = serverListeners.mangaListener.runningFold(emptyMap<Long, Manga>()) { manga, updatedMangaIds ->
            coroutineScope {
                manga + updatedMangaIds.filter { it in mangaIds.value }.map {
                    async {
                        getManga.await(it)
                    }
                }.awaitAll().filterNotNull().associateBy { it.id }
            }
        }.stateIn(this, SharingStarted.Eagerly, emptyMap())

        private val changedChapters = MutableStateFlow(emptyMap<Long, Chapter>())

        init {
            serverListeners.chapterIndexesListener
                .onEach { (mangaId, chapterIndexes) ->
                    if (chapterIndexes == null) {
                        val chapters = coroutineScope {
                            foldedUpdates.value.filterIsInstance<Updates.Update>().filter { it.manga.id == mangaId }.map {
                                async {
                                    getChapter.await(it.manga.id, it.chapter.index)
                                }
                            }.awaitAll().filterNotNull().associateBy { it.id }
                        }
                        changedChapters.update { it + chapters }
                    } else {
                        val chapters = coroutineScope {
                            chapterIndexes.mapNotNull { index -> chapterIds.value.find { it.first == mangaId && it.second == index } }
                                .map {
                                    async {
                                        getChapter.await(it.first, it.second)
                                    }
                                }.awaitAll().filterNotNull().associateBy { it.id }
                        }
                        changedChapters.update { it + chapters }
                    }
                }
                .launchIn(this)
            serverListeners.chapterIdsListener
                .onEach { (_, updatedChapterIds) ->
                    val chapters = coroutineScope {
                        updatedChapterIds.mapNotNull { id -> chapterIds.value.find { it.third == id } }.map {
                            async {
                                getChapter.await(it.first, it.second)
                            }
                        }.awaitAll().filterNotNull().associateBy { it.id }
                    }
                    changedChapters.update { it + chapters }
                }
                .launchIn(this)
        }

        val updates = combine(
            foldedUpdates,
            changedManga,
            changedChapters,
        ) { updates, changedManga, changedChapters ->
            updates.map {
                when (it) {
                    is Updates.Date -> it

                    is Updates.Update -> it.copy(
                        manga = changedManga[it.manga.id] ?: it.manga,
                        chapter = changedChapters[it.chapter.id] ?: it.chapter,
                    )
                }
            }
        }.stateIn(this, SharingStarted.Eagerly, emptyList())

        private val currentPage = MutableStateFlow(0)
        private val hasNextPage = MutableStateFlow(true)

        @Immutable
        sealed class Updates {
            @Immutable
            data class Update(
                val manga: Manga,
                val chapter: Chapter,
            ) : Updates()

            @Immutable
            data class Date(
                val date: String,
            ) : Updates() {
                constructor(date: LocalDate) : this(date.toString())
            }
        }

        fun loadNextPage(
            onComplete: (() -> Unit)? = null,
            onError: suspend (Throwable) -> Unit,
        ) {
            launch {
                if (hasNextPage.value && updatesMutex.tryLock()) {
                    currentPage.value++
                    if (!getUpdates(currentPage.value, onError)) {
                        currentPage.value--
                    }
                    updatesMutex.unlock()
                }
                onComplete?.invoke()
            }
        }

        private suspend fun getUpdates(
            page: Int,
            onError: suspend (Throwable) -> Unit,
        ): Boolean {
            val updates = getRecentUpdates.await(page, onError) ?: return false
            hasNextPage.value = updates.hasNextPage
            fetchedUpdates.emit(updates.page)
            return true
        }
    }
