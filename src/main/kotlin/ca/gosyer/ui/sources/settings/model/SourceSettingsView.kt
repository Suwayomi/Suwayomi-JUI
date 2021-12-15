/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.settings.model

import ca.gosyer.data.models.sourcepreference.CheckBoxPreference
import ca.gosyer.data.models.sourcepreference.EditTextPreference
import ca.gosyer.data.models.sourcepreference.ListPreference
import ca.gosyer.data.models.sourcepreference.MultiSelectListPreference
import ca.gosyer.data.models.sourcepreference.SourcePreference
import ca.gosyer.data.models.sourcepreference.SwitchPreference
import ca.gosyer.data.models.sourcepreference.TwoStateProps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Formatter
import kotlin.collections.List as KtList

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
                ?: false
        )
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
        override val props: TwoStateProps
    ) : TwoState(props) {
        internal constructor(index: Int, preference: CheckBoxPreference) : this(
            index,
            preference.props.title,
            preference.props.summary,
            preference.props
        )
    }

    data class Switch internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        override val props: TwoStateProps
    ) : TwoState(props) {
        internal constructor(index: Int, preference: SwitchPreference) : this(
            index,
            preference.props.title,
            preference.props.summary,
            preference.props
        )
    }

    data class List internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        override val props: ListPreference.ListProps
    ) : SourceSettingsView<ListPreference.ListProps, String>() {
        private val _state = MutableStateFlow(
            props.currentValue ?: props.defaultValue ?: "0"
        )
        override val state: StateFlow<String> = _state.asStateFlow()
        override fun updateState(value: String) {
            _state.value = value
        }
        internal constructor(index: Int, preference: ListPreference) : this(
            index,
            preference.props.title,
            preference.props.summary,
            preference.props
        )

        override val summary: String?
            get() = subtitle?.let { withFormat(it, props.entries[props.entryValues.indexOf(state.value)]) }

        fun getOptions() = props.entryValues.mapIndexed { index, s ->
            s to props.entries[index]
        }
    }

    data class MultiSelect internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        override val props: MultiSelectListPreference.MultiSelectListProps
    ) : SourceSettingsView<MultiSelectListPreference.MultiSelectListProps, KtList<String>?>() {
        private val _state = MutableStateFlow(
            props.currentValue ?: props.defaultValue
        )
        override val state: StateFlow<KtList<String>?> = _state.asStateFlow()
        override fun updateState(value: KtList<String>?) {
            _state.value = value
        }
        internal constructor(index: Int, preference: MultiSelectListPreference) : this(
            index,
            preference.props.title,
            preference.props.summary,
            preference.props
        )

        fun getOptions() = props.entryValues.mapIndexed { index, s ->
            s to props.entries[index]
        }

        fun toggleOption(key: String) {
            if (key in state.value.orEmpty()) {
                updateState(state.value.orEmpty() - key)
            } else {
                updateState(state.value.orEmpty() + key)
            }
        }
    }

    data class EditText internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        val dialogTitle: String?,
        val dialogMessage: String?,
        override val props: EditTextPreference.EditTextProps
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
            preference.props
        )
    }
}

private fun withFormat(text: String, value: Any?): String {
    return Formatter().use {
        it.format(text, value).toString()
    }
}

fun SourceSettingsView(index: Int, preference: SourcePreference): SourceSettingsView<*, *> {
    return when (preference) {
        is CheckBoxPreference -> SourceSettingsView.CheckBox(index, preference)
        is SwitchPreference -> SourceSettingsView.Switch(index, preference)
        is ListPreference -> SourceSettingsView.List(index, preference)
        is MultiSelectListPreference -> SourceSettingsView.MultiSelect(index, preference)
        is EditTextPreference -> SourceSettingsView.EditText(index, preference)
    }
}
