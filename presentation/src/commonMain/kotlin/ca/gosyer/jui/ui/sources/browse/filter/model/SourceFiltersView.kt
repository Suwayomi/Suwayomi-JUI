/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.browse.filter.model

import ca.gosyer.jui.domain.source.model.sourcefilters.CheckBoxFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.GroupFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.HeaderFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.SelectFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.SeparatorFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.SortFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.TextFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.TriStateFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SourceFiltersView<T, R : Any?> {
    abstract val index: Int
    abstract val name: String
    abstract val state: StateFlow<R>
    abstract fun updateState(value: R)

    abstract val filter: T

    data class CheckBox internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: CheckBoxFilter.CheckBoxProps,
        private val _state: MutableStateFlow<Boolean> = MutableStateFlow(filter.state),
    ) : SourceFiltersView<CheckBoxFilter.CheckBoxProps, Boolean>() {
        override val state: StateFlow<Boolean> = _state.asStateFlow()
        override fun updateState(value: Boolean) {
            _state.value = value
        }
        internal constructor(index: Int, filter: CheckBoxFilter) : this(
            index,
            filter.filter.name,
            filter.filter,
        )
    }

    data class Header internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: HeaderFilter.HeaderProps,
    ) : SourceFiltersView<HeaderFilter.HeaderProps, Any>() {
        override val state: StateFlow<Any> = MutableStateFlow(filter.state).asStateFlow()
        override fun updateState(value: Any) {
            // NO-OP
        }
        internal constructor(index: Int, filter: HeaderFilter) : this(
            index,
            filter.filter.name,
            filter.filter,
        )
    }
    data class Separator internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: SeparatorFilter.SeparatorProps,
    ) : SourceFiltersView<SeparatorFilter.SeparatorProps, Any>() {
        override val state: StateFlow<Any> = MutableStateFlow(filter.state).asStateFlow()
        override fun updateState(value: Any) {
            // NO-OP
        }
        internal constructor(index: Int, filter: SeparatorFilter) : this(
            index,
            filter.filter.name,
            filter.filter,
        )
    }

    data class Text internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: TextFilter.TextProps,
    ) : SourceFiltersView<TextFilter.TextProps, String>() {
        private val _state = MutableStateFlow(filter.state)
        override val state: StateFlow<String> = _state.asStateFlow()
        override fun updateState(value: String) {
            _state.value = value
        }
        internal constructor(index: Int, preference: TextFilter) : this(
            index,
            preference.filter.name,
            preference.filter,
        )
    }

    data class TriState internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: TriStateFilter.TriStateProps,
        private val _state: MutableStateFlow<Int> = MutableStateFlow(filter.state),
    ) : SourceFiltersView<TriStateFilter.TriStateProps, Int>() {
        override val state: StateFlow<Int> = _state.asStateFlow()
        override fun updateState(value: Int) {
            _state.value = value
        }
        internal constructor(index: Int, filter: TriStateFilter) : this(
            index,
            filter.filter.name,
            filter.filter,
        )
    }

    data class Select internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: SelectFilter.SelectProps,
        private val _state: MutableStateFlow<Int> = MutableStateFlow(filter.state),
    ) : SourceFiltersView<SelectFilter.SelectProps, Int>() {
        override val state: StateFlow<Int> = _state.asStateFlow()
        override fun updateState(value: Int) {
            _state.value = value
        }
        internal constructor(index: Int, filter: SelectFilter) : this(
            index,
            filter.filter.name,
            filter.filter,
        )
    }
    data class Sort internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: SortFilter.SortProps,
        private val _state: MutableStateFlow<SortFilter.Selection?> = MutableStateFlow(filter.state),
    ) : SourceFiltersView<SortFilter.SortProps, SortFilter.Selection?>() {
        override val state: StateFlow<SortFilter.Selection?> = _state.asStateFlow()
        override fun updateState(value: SortFilter.Selection?) {
            _state.value = value
        }
        internal constructor(index: Int, filter: SortFilter) : this(
            index,
            filter.filter.name,
            filter.filter,
        )
    }

    data class Group internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: GroupFilter.GroupProps,
    ) : SourceFiltersView<GroupFilter.GroupProps, List<SourceFiltersView<*, *>>>() {
        override val state: StateFlow<List<SourceFiltersView<*, *>>> = MutableStateFlow(
            filter.state.mapIndexed { itemIndex, sourceFilter ->
                SourceFiltersView(itemIndex, sourceFilter)
            },
        ).asStateFlow()
        override fun updateState(value: List<SourceFiltersView<*, *>>) {
            // NO-OP
        }
        internal constructor(index: Int, filter: GroupFilter) : this(
            index,
            filter.filter.name,
            filter.filter,
        )
    }
}

@Suppress("FunctionName")
fun SourceFiltersView(index: Int, sourceFilter: SourceFilter): SourceFiltersView<*, *> {
    return when (sourceFilter) {
        is CheckBoxFilter -> SourceFiltersView.CheckBox(index, sourceFilter)
        is HeaderFilter -> SourceFiltersView.Header(index, sourceFilter)
        is SeparatorFilter -> SourceFiltersView.Separator(index, sourceFilter)
        is TextFilter -> SourceFiltersView.Text(index, sourceFilter)
        is TriStateFilter -> SourceFiltersView.TriState(index, sourceFilter)
        is SelectFilter -> SourceFiltersView.Select(index, sourceFilter)
        is SortFilter -> SourceFiltersView.Sort(index, sourceFilter)
        is GroupFilter -> SourceFiltersView.Group(index, sourceFilter)
    }
}
