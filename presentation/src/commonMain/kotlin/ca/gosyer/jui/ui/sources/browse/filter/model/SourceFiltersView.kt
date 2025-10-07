/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.browse.filter.model

import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Suppress("DATA_CLASS_COPY_VISIBILITY_WILL_BE_CHANGED_WARNING")
sealed class SourceFiltersView<T : SourceFilter, R : Any?> {
    abstract val index: Int
    abstract val name: String
    abstract val state: StateFlow<R>

    abstract fun updateState(value: R)

    abstract val filter: T

    data class CheckBox internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: SourceFilter.Checkbox,
        private val _state: MutableStateFlow<Boolean> = MutableStateFlow(filter.value),
    ) : SourceFiltersView<SourceFilter.Checkbox, Boolean>() {
        override val state: StateFlow<Boolean> = _state.asStateFlow()

        override fun updateState(value: Boolean) {
            _state.value = value
        }
        internal constructor(index: Int, filter: SourceFilter.Checkbox) : this(
            index,
            filter.name,
            filter,
        )
    }

    data class Header internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: SourceFilter.Header,
    ) : SourceFiltersView<SourceFilter.Header, Any>() {
        override val state: StateFlow<Any> = MutableStateFlow(filter.name).asStateFlow()

        override fun updateState(value: Any) {
            // NO-OP
        }
        internal constructor(index: Int, filter: SourceFilter.Header) : this(
            index,
            filter.name,
            filter,
        )
    }

    data class Separator internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: SourceFilter.Separator,
    ) : SourceFiltersView<SourceFilter.Separator, Any>() {
        override val state: StateFlow<Any> = MutableStateFlow(filter.name).asStateFlow()

        override fun updateState(value: Any) {
            // NO-OP
        }
        internal constructor(index: Int, filter: SourceFilter.Separator) : this(
            index,
            filter.name,
            filter,
        )
    }

    data class Text internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: SourceFilter.Text,
    ) : SourceFiltersView<SourceFilter.Text, String>() {
        private val _state = MutableStateFlow(filter.value)
        override val state: StateFlow<String> = _state.asStateFlow()

        override fun updateState(value: String) {
            _state.value = value
        }
        internal constructor(index: Int, preference: SourceFilter.Text) : this(
            index,
            preference.name,
            preference,
        )
    }

    data class TriState internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: SourceFilter.TriState,
        private val _state: MutableStateFlow<SourceFilter.TriState.TriStateValue> = MutableStateFlow(filter.value),
    ) : SourceFiltersView<SourceFilter.TriState, SourceFilter.TriState.TriStateValue>() {
        override val state: StateFlow<SourceFilter.TriState.TriStateValue> = _state.asStateFlow()

        override fun updateState(value: SourceFilter.TriState.TriStateValue) {
            _state.value = value
        }
        internal constructor(index: Int, filter: SourceFilter.TriState) : this(
            index,
            filter.name,
            filter,
        )
    }

    data class Select internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: SourceFilter.Select,
        private val _state: MutableStateFlow<Int> = MutableStateFlow(filter.value),
    ) : SourceFiltersView<SourceFilter.Select, Int>() {
        override val state: StateFlow<Int> = _state.asStateFlow()

        override fun updateState(value: Int) {
            _state.value = value
        }
        internal constructor(index: Int, filter: SourceFilter.Select) : this(
            index,
            filter.name,
            filter,
        )
    }

    data class Sort internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: SourceFilter.Sort,
        private val _state: MutableStateFlow<SourceFilter.Sort.SelectionChange?> = MutableStateFlow(filter.value),
    ) : SourceFiltersView<SourceFilter.Sort, SourceFilter.Sort.SelectionChange?>() {
        override val state: StateFlow<SourceFilter.Sort.SelectionChange?> = _state.asStateFlow()

        override fun updateState(value: SourceFilter.Sort.SelectionChange?) {
            _state.value = value
        }
        internal constructor(index: Int, filter: SourceFilter.Sort) : this(
            index,
            filter.name,
            filter,
        )
    }

    data class Group internal constructor(
        override val index: Int,
        override val name: String,
        override val filter: SourceFilter.Group,
    ) : SourceFiltersView<SourceFilter.Group, List<SourceFiltersView<*, *>>>() {
        override val state: StateFlow<List<SourceFiltersView<*, *>>> = MutableStateFlow(
            filter.value.mapIndexed { itemIndex, sourceFilter ->
                SourceFiltersView(itemIndex, sourceFilter)
            },
        ).asStateFlow()

        override fun updateState(value: List<SourceFiltersView<*, *>>) {
            // NO-OP
        }
        internal constructor(index: Int, filter: SourceFilter.Group) : this(
            index,
            filter.name,
            filter,
        )
    }
}

fun SourceFiltersView(
    index: Int,
    sourceFilter: SourceFilter,
): SourceFiltersView<*, *> =
    when (sourceFilter) {
        is SourceFilter.Checkbox -> SourceFiltersView.CheckBox(index, sourceFilter)
        is SourceFilter.Header -> SourceFiltersView.Header(index, sourceFilter)
        is SourceFilter.Separator -> SourceFiltersView.Separator(index, sourceFilter)
        is SourceFilter.Text -> SourceFiltersView.Text(index, sourceFilter)
        is SourceFilter.TriState -> SourceFiltersView.TriState(index, sourceFilter)
        is SourceFilter.Select -> SourceFiltersView.Select(index, sourceFilter)
        is SourceFilter.Sort -> SourceFiltersView.Sort(index, sourceFilter)
        is SourceFilter.Group -> SourceFiltersView.Group(index, sourceFilter)
    }
