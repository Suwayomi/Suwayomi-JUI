/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import ca.gosyer.jui.core.prefs.getAsFlow
import ca.gosyer.jui.domain.category.interactor.GetCategories
import ca.gosyer.jui.domain.category.interactor.GetMangaListFromCategory
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.library.interactor.RemoveMangaFromLibrary
import ca.gosyer.jui.domain.library.model.FilterState
import ca.gosyer.jui.domain.library.model.Sort
import ca.gosyer.jui.domain.library.service.LibraryPreferences
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.manga.model.MangaStatus
import ca.gosyer.jui.domain.updates.interactor.UpdateCategory
import ca.gosyer.jui.domain.updates.interactor.UpdateLibrary
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.state.SavedStateHandle
import ca.gosyer.jui.ui.base.state.getStateFlow
import ca.gosyer.jui.ui.util.lang.CollatorComparator
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import com.diamondedge.logging.logging

@Stable
sealed class LibraryState {
    @Stable
    data object Loading : LibraryState()

    @Stable
    data class Failed(
        val e: Throwable,
    ) : LibraryState()

    @Stable
    data class Loaded(
        val categories: ImmutableList<Category>,
    ) : LibraryState()
}

@Stable
sealed class CategoryState {
    @Stable
    data object Loading : CategoryState()

    @Stable
    data class Failed(
        val e: Throwable,
    ) : CategoryState()

    @Stable
    data class Loaded(
        val items: StateFlow<ImmutableList<Manga>>,
        val unfilteredItems: MutableStateFlow<ImmutableList<Manga>>,
    ) : CategoryState()
}

private typealias LibraryMap = MutableMap<Long, MutableStateFlow<CategoryState>>

private data class Library(
    val categories: MutableStateFlow<LibraryState>,
    val mangaMap: LibraryMap,
)

private fun LibraryMap.getManga(id: Long) =
    getOrPut(id) {
        MutableStateFlow(CategoryState.Loading)
    }

private fun LibraryMap.setError(
    id: Long,
    e: Throwable,
) {
    getManga(id).value = CategoryState.Failed(e)
}

private fun LibraryMap.setManga(
    id: Long,
    manga: ImmutableList<Manga>,
    getItemsFlow: (StateFlow<List<Manga>>) -> StateFlow<ImmutableList<Manga>>,
) {
    val flow = getManga(id)
    when (val state = flow.value) {
        is CategoryState.Loaded -> state.unfilteredItems.value = manga

        else -> {
            val unfilteredItems = MutableStateFlow(manga)
            flow.value = CategoryState.Loaded(getItemsFlow(unfilteredItems), unfilteredItems)
        }
    }
}

@Inject
class LibraryScreenViewModel(
    private val getCategories: GetCategories,
    private val getMangaListFromCategory: GetMangaListFromCategory,
    private val removeMangaFromLibrary: RemoveMangaFromLibrary,
    private val updateLibrary: UpdateLibrary,
    private val updateCategory: UpdateCategory,
    libraryPreferences: LibraryPreferences,
    contextWrapper: ContextWrapper,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel(contextWrapper) {
    private val library = Library(MutableStateFlow(LibraryState.Loading), mutableMapOf())
    val categories = library.categories.asStateFlow()

    private val _selectedCategoryIndex by savedStateHandle.getStateFlow { 0 }
    val selectedCategoryIndex = _selectedCategoryIndex.asStateFlow()

    private val _showingMenu by savedStateHandle.getStateFlow { false }
    val showingMenu = _showingMenu.asStateFlow()

    val displayMode = libraryPreferences.displayMode().stateIn(scope)
    val gridColumns = libraryPreferences.gridColumns().stateIn(scope)
    val gridSize = libraryPreferences.gridSize().stateIn(scope)
    val unreadBadges = libraryPreferences.unreadBadge().stateIn(scope)
    val downloadBadges = libraryPreferences.downloadBadge().stateIn(scope)
    val languageBadges = libraryPreferences.languageBadge().stateIn(scope)
    val localBadges = libraryPreferences.localBadge().stateIn(scope)

    private val sortMode = libraryPreferences.sortMode().stateIn(scope)
    private val sortAscending = libraryPreferences.sortAscending().stateIn(scope)

    private val filter: Flow<(Manga) -> Boolean> = combine(
        libraryPreferences.filterDownloaded().getAsFlow(),
        libraryPreferences.filterUnread().getAsFlow(),
        libraryPreferences.filterCompleted().getAsFlow(),
    ) { downloaded, unread, completed ->
        { manga ->
            when (downloaded) {
                FilterState.EXCLUDED -> manga.downloadCount == null || manga.downloadCount == 0
                FilterState.INCLUDED -> manga.downloadCount != null && (manga.downloadCount ?: 0) > 0
                FilterState.IGNORED -> true
            } &&
                when (unread) {
                    FilterState.EXCLUDED -> manga.unreadCount == null || manga.unreadCount == 0
                    FilterState.INCLUDED -> manga.unreadCount != null && (manga.unreadCount ?: 0) > 0
                    FilterState.IGNORED -> true
                } &&
                when (completed) {
                    FilterState.EXCLUDED -> manga.status != MangaStatus.COMPLETED
                    FilterState.INCLUDED -> manga.status == MangaStatus.COMPLETED
                    FilterState.IGNORED -> true
                }
        }
    }

    private val _query by savedStateHandle.getStateFlow { "" }
    val query = _query.asStateFlow()

    private val comparator = combine(sortMode, sortAscending) { sortMode, sortAscending ->
        getComparator(sortMode, sortAscending)
    }.stateIn(scope, SharingStarted.Eagerly, compareBy { it.title })

    init {
        getLibrary()
    }

    private fun getLibrary() {
        library.categories.value = LibraryState.Loading
        getCategories.asFlow()
            .onEach { categories ->
                if (categories.isEmpty()) {
                    throw Exception(MR.strings.library_empty.toPlatformString())
                }
                library.categories.value = LibraryState.Loaded(
                    categories.sortedBy { it.order }
                        .toImmutableList(),
                )
                categories.forEach { category ->
                    getMangaListFromCategory.asFlow(category)
                        .onEach {
                            library.mangaMap.setManga(
                                id = category.id,
                                manga = it.toImmutableList(),
                                getItemsFlow = ::getMangaItemsFlow,
                            )
                        }
                        .catch {
                            log.warn(it) { "Failed to get manga list from category ${category.name}" }
                            library.mangaMap.setError(category.id, it)
                        }
                        .launchIn(scope)
                }
            }
            .catch {
                library.categories.value = LibraryState.Failed(it)
                log.warn(it) { "Failed to get categories" }
            }
            .launchIn(scope)
    }

    fun setSelectedPage(page: Int) {
        _selectedCategoryIndex.value = page
    }

    fun setShowingMenu(showingMenu: Boolean) {
        _showingMenu.value = showingMenu
    }

    private fun getComparator(
        sortMode: Sort,
        ascending: Boolean,
    ): Comparator<Manga> {
        val sortFn: (Manga, Manga) -> Int = when (sortMode) {
            Sort.ALPHABETICAL -> {
                val locale = Locale.current
                val collator = CollatorComparator(locale);

                { a, b ->
                    collator.compare(a.title.toLowerCase(locale), b.title.toLowerCase(locale))
                }
            }

            Sort.UNREAD -> {
                { a, b ->
                    when {
                        // Ensure unread content comes first
                        (a.unreadCount ?: 0) == (b.unreadCount ?: 0) -> 0

                        a.unreadCount == null || a.unreadCount == 0 -> if (ascending) 1 else -1

                        b.unreadCount == null || b.unreadCount == 0 -> if (ascending) -1 else 1

                        else -> (a.unreadCount ?: 0).compareTo(b.unreadCount ?: 0)
                    }
                }
            }

            Sort.DATE_ADDED -> {
                { a, b ->
                    a.inLibraryAt.compareTo(b.inLibraryAt)
                }
            }
        }
        return if (ascending) {
            Comparator(sortFn)
        } else {
            Comparator(sortFn).reversed()
        }
    }

    private suspend fun filterManga(
        query: String,
        mangaList: List<Manga>,
    ): List<Manga> {
        if (query.isBlank()) return mangaList
        val queries = query.split(" ")
        return mangaList.asFlow()
            .filter { manga ->
                queries.all { query ->
                    manga.title.contains(query, true) ||
                        manga.author.orEmpty().contains(query, true) ||
                        manga.artist.orEmpty().contains(query, true) ||
                        manga.genre.any { it.contains(query, true) } ||
                        manga.description.orEmpty().contains(query, true) ||
                        manga.status.name.contains(query, true)
                }
            }
            .cancellable()
            .buffer()
            .toList()
    }

    private fun getMangaItemsFlow(unfilteredItemsFlow: StateFlow<List<Manga>>): StateFlow<ImmutableList<Manga>> =
        combine(
            unfilteredItemsFlow,
            query,
        ) {
                unfilteredItems,
                query,
            ->
            filterManga(query, unfilteredItems)
        }.combine(filter) { filteredManga, filterer ->
            filteredManga.filter(filterer)
        }.combine(comparator) { filteredManga, comparator ->
            filteredManga.sortedWith(comparator)
        }.map {
            it.toImmutableList()
        }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

    fun getLibraryForCategoryId(id: Long): StateFlow<CategoryState> = library.mangaMap.getManga(id)

    private fun getCategoriesToUpdate(mangaId: Long): List<Category> =
        library.mangaMap
            .filter { mangaMapEntry ->
                (mangaMapEntry.value.value as? CategoryState.Loaded)?.items?.value?.firstOrNull { it.id == mangaId } != null
            }
            .mapNotNull { (id) -> (library.categories.value as? LibraryState.Loaded)?.categories?.first { it.id == id } }

    fun removeManga(mangaId: Long) {
        scope.launch {
            removeMangaFromLibrary.await(mangaId, onError = { toast(it.message.orEmpty()) })
        }
    }

    fun updateQuery(query: String) {
        _query.value = query
    }

    fun updateLibrary() {
        scope.launch { updateLibrary.await(onError = { toast(it.message.orEmpty()) }) }
    }

    fun updateCategory(category: Category) {
        scope.launch { updateCategory.await(category, onError = { toast(it.message.orEmpty()) }) }
    }

    private companion object {
        private val log = logging()
    }
}
