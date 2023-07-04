/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.settings.model

import ca.gosyer.jui.domain.source.model.sourcepreference.CheckBoxPreference
import ca.gosyer.jui.domain.source.model.sourcepreference.EditTextPreference
import ca.gosyer.jui.domain.source.model.sourcepreference.ListPreference
import ca.gosyer.jui.domain.source.model.sourcepreference.MultiSelectListPreference
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.SwitchPreference
import ca.gosyer.jui.domain.source.model.sourcepreference.TwoStateProps
import ca.gosyer.jui.ui.util.lang.stringFormat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SourceSettingsView<T, R : Any?> {
    abstract val index: Int
    abstract val title: String?
    abstract val subtitle: String?
    abstract val state: StateFlow<R>
    abstract fun updateState(value: R)

    abstract val props: T

    open val summary: String?
        get() = subtitle?.let { withFormat(it, state.value) }

    sealed class TwoState(
        props: TwoStateProps,
        private val _state: MutableStateFlow<Boolean> = MutableStateFlow(
            props.currentValue
                ?: props.defaultValue
                ?: false,
        ),
    ) : SourceSettingsView<TwoStateProps, Boolean>() {
        override val state: StateFlow<Boolean> = _state.asStateFlow()
        override fun updateState(value: Boolean) {
            _state.value = value
        }
    }

    data class CheckBox internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        override val props: TwoStateProps,
    ) : TwoState(props) {
        internal constructor(index: Int, preference: CheckBoxPreference) : this(
            index,
            preference.props.title,
            preference.props.summary,
            preference.props,
        )
    }

    data class Switch internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        override val props: TwoStateProps,
    ) : TwoState(props) {
        internal constructor(index: Int, preference: SwitchPreference) : this(
            index,
            preference.props.title,
            preference.props.summary,
            preference.props,
        )
    }

    data class List internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        override val props: ListPreference.ListProps,
    ) : SourceSettingsView<ListPreference.ListProps, String>() {
        private val _state = MutableStateFlow(
            props.currentValue ?: props.defaultValue ?: "0",
        )
        override val state: StateFlow<String> = _state.asStateFlow()
        override fun updateState(value: String) {
            _state.value = value
        }
        internal constructor(index: Int, preference: ListPreference) : this(
            index,
            preference.props.title,
            preference.props.summary,
            preference.props,
        )

        override val summary: String?
            get() = subtitle?.let { withFormat(it, props.entries[props.entryValues.indexOf(state.value)]) }

        fun getOptions() =
            props.entryValues.mapIndexed { index, s ->
                s to props.entries[index]
            }.toImmutableList()
    }

    data class MultiSelect internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        override val props: MultiSelectListPreference.MultiSelectListProps,
    ) : SourceSettingsView<MultiSelectListPreference.MultiSelectListProps, ImmutableList<String>?>() {
        private val _state = MutableStateFlow(
            props.currentValue?.toImmutableList() ?: props.defaultValue?.toImmutableList(),
        )
        override val state: StateFlow<ImmutableList<String>?> = _state.asStateFlow()
        override fun updateState(value: ImmutableList<String>?) {
            _state.value = value
        }
        internal constructor(index: Int, preference: MultiSelectListPreference) : this(
            index,
            preference.props.title,
            preference.props.summary,
            preference.props,
        )

        fun getOptions() =
            props.entryValues.mapIndexed { index, s ->
                s to props.entries[index]
            }.toImmutableList()

        fun toggleOption(key: String) {
            if (key in state.value.orEmpty()) {
                updateState(state.value.orEmpty().toPersistentList() - key)
            } else {
                updateState(state.value.orEmpty().toPersistentList() + key)
            }
        }
    }

    data class EditText internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        val dialogTitle: String?,
        val dialogMessage: String?,
        override val props: EditTextPreference.EditTextProps,
    ) : SourceSettingsView<EditTextPreference.EditTextProps, String>() {
        private val _state = MutableStateFlow(props.currentValue ?: props.defaultValue.orEmpty())
        override val state: StateFlow<String> = _state.asStateFlow()
        override fun updateState(value: String) {
            _state.value = value
        }
        internal constructor(index: Int, preference: EditTextPreference) : this(
            index,
            preference.props.title,
            preference.props.summary,
            preference.props.dialogTitle,
            preference.props.dialogMessage,
            preference.props,
        )
    }
}

private fun withFormat(
    text: String,
    value: Any?,
): String {
    return stringFormat(text, value)
}

fun SourceSettingsView(
    index: Int,
    preference: SourcePreference,
): SourceSettingsView<*, *> {
    return when (preference) {
        is CheckBoxPreference -> SourceSettingsView.CheckBox(index, preference)
        is SwitchPreference -> SourceSettingsView.Switch(index, preference)
        is ListPreference -> SourceSettingsView.List(index, preference)
        is MultiSelectListPreference -> SourceSettingsView.MultiSelect(index, preference)
        is EditTextPreference -> SourceSettingsView.EditText(index, preference)
    }
}
