package ca.gosyer.jui.data.source

import ca.gosyer.jui.data.graphql.FetchLatestMangaMutation
import ca.gosyer.jui.data.graphql.FetchPopularMangaMutation
import ca.gosyer.jui.data.graphql.FetchSearchMangaMutation
import ca.gosyer.jui.data.graphql.GetSourceFiltersQuery
import ca.gosyer.jui.data.graphql.GetSourceListQuery
import ca.gosyer.jui.data.graphql.GetSourcePreferencesQuery
import ca.gosyer.jui.data.graphql.GetSourceQuery
import ca.gosyer.jui.data.graphql.SetSourceSettingMutation
import ca.gosyer.jui.data.graphql.fragment.SourceFragment
import ca.gosyer.jui.data.graphql.type.FilterChangeInput
import ca.gosyer.jui.data.graphql.type.SortSelectionInput
import ca.gosyer.jui.data.graphql.type.SourcePreferenceChangeInput
import ca.gosyer.jui.data.graphql.type.TriState
import ca.gosyer.jui.data.manga.MangaRepositoryImpl.Companion.toManga
import ca.gosyer.jui.data.util.toOptional
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.source.model.MangaPage
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilter.TriState.TriStateValue
import ca.gosyer.jui.domain.source.model.sourcepreference.CheckBoxSourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.EditTextSourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.ListSourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.MultiSelectListSourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.SwitchSourcePreference
import ca.gosyer.jui.domain.source.service.SourceRepository
import com.apollographql.apollo.ApolloClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SourceRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val http: Http,
    private val serverUrl: Url,
) : SourceRepository {
    override fun getSourceList(): Flow<List<Source>> =
        apolloClient.query(
            GetSourceListQuery(),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.sources.nodes.map { it.sourceFragment.toSource() }
            }

    override fun getSourceInfo(sourceId: Long): Flow<Source> =
        apolloClient.query(
            GetSourceQuery(sourceId),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.source.sourceFragment.toSource()
            }

    override fun getPopularManga(
        sourceId: Long,
        pageNum: Int,
    ): Flow<MangaPage> =
        apolloClient.mutation(
            FetchPopularMangaMutation(sourceId, pageNum),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                MangaPage(
                    data.fetchSourceManga!!.mangas.map { it.mangaFragment.toManga() },
                    data.fetchSourceManga.hasNextPage,
                )
            }

    override fun getLatestManga(
        sourceId: Long,
        pageNum: Int,
    ): Flow<MangaPage> =
        apolloClient.mutation(
            FetchLatestMangaMutation(sourceId, pageNum),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                MangaPage(
                    data.fetchSourceManga!!.mangas.map { it.mangaFragment.toManga() },
                    data.fetchSourceManga.hasNextPage,
                )
            }

    override fun getFilterList(sourceId: Long): Flow<List<SourceFilter>> =
        apolloClient.query(
            GetSourceFiltersQuery(sourceId),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.source.filters.mapIndexed { index, filter ->
                    when (filter.__typename) {
                        "CheckBoxFilter" -> {
                            val filter = filter.onCheckBoxFilter!!
                            SourceFilter.Checkbox(
                                index,
                                filter.name,
                                filter.checkBoxFilterDefault,
                            )
                        }

                        "HeaderFilter" -> {
                            val filter = filter.onHeaderFilter!!
                            SourceFilter.Header(
                                index,
                                filter.name,
                            )
                        }

                        "SelectFilter" -> {
                            val filter = filter.onSelectFilter!!
                            SourceFilter.Select(
                                index,
                                filter.name,
                                filter.values,
                                filter.selectFilterDefault,
                            )
                        }

                        "TriStateFilter" -> {
                            val filter = filter.onTriStateFilter!!
                            SourceFilter.TriState(
                                index,
                                filter.name,
                                when (filter.triStateFilterDefault) {
                                    TriState.IGNORE -> TriStateValue.IGNORE
                                    TriState.INCLUDE -> TriStateValue.INCLUDE
                                    TriState.EXCLUDE -> TriStateValue.EXCLUDE
                                    TriState.UNKNOWN__ -> TriStateValue.IGNORE
                                },
                            )
                        }

                        "TextFilter" -> {
                            val filter = filter.onTextFilter!!
                            SourceFilter.Text(
                                index,
                                filter.name,
                                filter.textFilterDefault,
                            )
                        }

                        "SortFilter" -> {
                            val filter = filter.onSortFilter!!
                            SourceFilter.Sort(
                                index,
                                filter.name,
                                filter.values,
                                filter.sortFilterDefault?.let {
                                    SourceFilter.Sort.SelectionChange(it.ascending, it.index)
                                },
                            )
                        }

                        "SeparatorFilter" -> {
                            val filter = filter.onSeparatorFilter!!
                            SourceFilter.Separator(
                                index,
                                filter.name,
                            )
                        }

                        "GroupFilter" -> {
                            SourceFilter.Group(
                                index,
                                filter.onGroupFilter!!.name,
                                filter.onGroupFilter.filters.mapIndexed { index, filter ->
                                    when (filter.__typename) {
                                        "CheckBoxFilter" -> {
                                            val filter = filter.onCheckBoxFilter!!
                                            SourceFilter.Checkbox(
                                                index,
                                                filter.name,
                                                filter.checkBoxFilterDefault,
                                            )
                                        }

                                        "HeaderFilter" -> {
                                            val filter = filter.onHeaderFilter!!
                                            SourceFilter.Header(
                                                index,
                                                filter.name,
                                            )
                                        }

                                        "SelectFilter" -> {
                                            val filter = filter.onSelectFilter!!
                                            SourceFilter.Select(
                                                index,
                                                filter.name,
                                                filter.values,
                                                filter.selectFilterDefault,
                                            )
                                        }

                                        "TriStateFilter" -> {
                                            val filter = filter.onTriStateFilter!!
                                            SourceFilter.TriState(
                                                index,
                                                filter.name,
                                                when (filter.triStateFilterDefault) {
                                                    TriState.IGNORE -> TriStateValue.IGNORE
                                                    TriState.INCLUDE -> TriStateValue.INCLUDE
                                                    TriState.EXCLUDE -> TriStateValue.EXCLUDE
                                                    TriState.UNKNOWN__ -> TriStateValue.IGNORE
                                                },
                                            )
                                        }

                                        "TextFilter" -> {
                                            val filter = filter.onTextFilter!!
                                            SourceFilter.Text(
                                                index,
                                                filter.name,
                                                filter.textFilterDefault,
                                            )
                                        }

                                        "SortFilter" -> {
                                            val filter = filter.onSortFilter!!
                                            SourceFilter.Sort(
                                                index,
                                                filter.name,
                                                filter.values,
                                                filter.sortFilterDefault?.let {
                                                    SourceFilter.Sort.SelectionChange(it.ascending, it.index)
                                                },
                                            )
                                        }

                                        "SeparatorFilter" -> {
                                            val filter = filter.onSeparatorFilter!!
                                            SourceFilter.Separator(
                                                index,
                                                filter.name,
                                            )
                                        }

                                        else -> SourceFilter.Header(index, "")
                                    }
                                },
                            )
                        }

                        else -> SourceFilter.Header(index, "")
                    }
                }
            }

    fun SourceFilter.toFilterChange(): List<FilterChangeInput>? {
        return when (this) {
            is SourceFilter.Checkbox -> if (value != default) {
                listOf(FilterChangeInput(position = position, checkBoxState = value.toOptional()))
            } else {
                null
            }

            is SourceFilter.Header -> null

            is SourceFilter.Select -> if (value != default) {
                listOf(FilterChangeInput(position = position, selectState = value.toOptional()))
            } else {
                null
            }

            is SourceFilter.Separator -> null

            is SourceFilter.Sort -> if (value != default) {
                listOf(
                    FilterChangeInput(
                        position = position,
                        sortState = value?.let { SortSelectionInput(it.ascending, it.index) }.toOptional(),
                    ),
                )
            } else {
                null
            }

            is SourceFilter.Text -> if (value != default) {
                listOf(FilterChangeInput(position = position, textState = value.toOptional()))
            } else {
                null
            }

            is SourceFilter.TriState -> if (value != default) {
                listOf(
                    FilterChangeInput(
                        position = position,
                        triState = when (value) {
                            TriStateValue.IGNORE -> TriState.IGNORE
                            TriStateValue.INCLUDE -> TriState.INCLUDE
                            TriStateValue.EXCLUDE -> TriState.EXCLUDE
                        }.toOptional(),
                    ),
                )
            } else {
                null
            }

            is SourceFilter.Group -> value.mapNotNull {
                FilterChangeInput(
                    position = position,
                    groupChange = it.toFilterChange()
                        ?.firstOrNull()
                        ?.toOptional()
                        ?: return@mapNotNull null,
                )
            }
        }
    }

    override fun getSearchResults(
        sourceId: Long,
        pageNum: Int,
        searchTerm: String?,
        filters: List<SourceFilter>?,
    ): Flow<MangaPage> =
        apolloClient.mutation(
            FetchSearchMangaMutation(
                sourceId,
                pageNum,
                searchTerm.toOptional(),
                filters?.mapNotNull {
                    it.toFilterChange()
                }?.flatten().toOptional(),
            ),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                MangaPage(
                    data.fetchSourceManga!!.mangas.map { it.mangaFragment.toManga() },
                    data.fetchSourceManga.hasNextPage,
                )
            }

    override fun getSourceSettings(sourceId: Long): Flow<List<SourcePreference>> =
        apolloClient.query(
            GetSourcePreferencesQuery(sourceId),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.source.preferences.mapIndexedNotNull { index, preference ->
                    println("Test " + preference.__typename + " $index")
                    when (preference.__typename) {
                        "CheckBoxPreference" -> preference.onCheckBoxPreference!!.let {
                            CheckBoxSourcePreference(
                                position = index,
                                key = it.key,
                                title = it.checkBoxTitle,
                                summary = it.summary,
                                visible = it.visible,
                                enabled = it.enabled,
                                currentValue = it.checkBoxCheckBoxCurrentValue,
                                default = it.checkBoxDefault,
                            )
                        }

                        "EditTextPreference" -> preference.onEditTextPreference!!.let {
                            EditTextSourcePreference(
                                position = index,
                                key = it.key,
                                title = it.editTextPreferenceTitle,
                                summary = it.summary,
                                visible = it.visible,
                                enabled = it.enabled,
                                currentValue = it.editTextPreferenceCurrentValue,
                                default = it.editTextPreferenceDefault,
                                dialogTitle = it.dialogTitle,
                                dialogMessage = it.dialogMessage,
                                text = it.text,
                            )
                        }

                        "SwitchPreference" -> preference.onSwitchPreference!!.let {
                            SwitchSourcePreference(
                                position = index,
                                key = it.key,
                                title = it.switchPreferenceTitle,
                                summary = it.summary,
                                visible = it.visible,
                                enabled = it.enabled,
                                currentValue = it.switchPreferenceCurrentValue,
                                default = it.switchPreferenceDefault,
                            )
                        }

                        "MultiSelectListPreference" -> preference.onMultiSelectListPreference!!.let {
                            MultiSelectListSourcePreference(
                                position = index,
                                key = it.key,
                                title = it.multiSelectListPreferenceTitle,
                                summary = it.summary,
                                visible = it.visible,
                                enabled = it.enabled,
                                currentValue = it.multiSelectListPreferenceCurrentValue,
                                default = it.multiSelectListPreferenceDefault,
                                dialogTitle = it.dialogTitle,
                                dialogMessage = it.dialogMessage,
                                entries = it.entries,
                                entryValues = it.entryValues,
                            )
                        }

                        "ListPreference" -> preference.onListPreference!!.let {
                            ListSourcePreference(
                                position = index,
                                key = it.key,
                                title = it.listPreferenceTitle,
                                summary = it.summary,
                                visible = it.visible,
                                enabled = it.enabled,
                                currentValue = it.listPreferenceCurrentValue,
                                default = it.listPreferenceDefault,
                                entries = it.entries,
                                entryValues = it.entryValues,
                            )
                        }

                        else -> null
                    }
                }
            }

    override fun setSourceSetting(
        sourceId: Long,
        sourcePreference: SourcePreference,
    ): Flow<Unit> =
        apolloClient.mutation(
            SetSourceSettingMutation(
                sourceId,
                SourcePreferenceChangeInput(
                    position = sourcePreference.position,
                    checkBoxState = (sourcePreference as? CheckBoxSourcePreference)?.currentValue.toOptional(),
                    switchState = (sourcePreference as? SwitchSourcePreference)?.currentValue.toOptional(),
                    editTextState = (sourcePreference as? EditTextSourcePreference)?.currentValue.toOptional(),
                    multiSelectState = (sourcePreference as? MultiSelectListSourcePreference)?.currentValue.toOptional(),
                    listState = (sourcePreference as? ListSourcePreference)?.currentValue.toOptional(),
                ),
            ),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors.updateSourcePreference!!.clientMutationId
            }

    companion object {
        internal fun SourceFragment.toSource(): Source =
            Source(
                id,
                name,
                lang,
                iconUrl,
                supportsLatest,
                isConfigurable,
                isNsfw,
                displayName,
            )
    }
}
