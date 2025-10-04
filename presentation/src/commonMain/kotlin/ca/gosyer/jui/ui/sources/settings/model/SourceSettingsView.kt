/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.settings.model

import ca.gosyer.jui.domain.source.model.sourcepreference.CheckBoxSourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.EditTextSourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.ListSourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.MultiSelectListSourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.SwitchSourcePreference
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView.CheckBox
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView.EditText
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView.List
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView.MultiSelect
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView.Switch
import ca.gosyer.jui.ui.util.lang.stringFormat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SourceSettingsView<T : SourcePreference, R : Any?> {
    abstract val index: Int
    abstract val title: String?
    abstract val subtitle: String?
    abstract val state: StateFlow<R>

    abstract fun updateState(value: R)

    abstract val props: T

    open val summary: String?
        get() = subtitle?.let { withFormat(it, state.value) }

    data class CheckBox internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        override val props: CheckBoxSourcePreference,
    ) : SourceSettingsView<CheckBoxSourcePreference, Boolean>() {
        private val _state = MutableStateFlow(
            props.currentValue ?: props.default,
        )
        override val state: StateFlow<Boolean> = _state.asStateFlow()

        override fun updateState(value: Boolean) {
            _state.value = value
        }

        internal constructor(index: Int, preference: CheckBoxSourcePreference) : this(
            index,
            preference.title,
            preference.summary,
            preference,
        )
    }

    data class Switch internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        override val props: SwitchSourcePreference,
    ) : SourceSettingsView<SwitchSourcePreference, Boolean>() {
        private val _state = MutableStateFlow(
            props.currentValue ?: props.default,
        )
        override val state: StateFlow<Boolean> = _state.asStateFlow()

        override fun updateState(value: Boolean) {
            _state.value = value
        }

        internal constructor(index: Int, preference: SwitchSourcePreference) : this(
            index,
            preference.title,
            preference.summary,
            preference,
        )
    }

    data class List internal constructor(
        override val index: Int,
        override val title: String?,
        override val subtitle: String?,
        override val props: ListSourcePreference,
    ) : SourceSettingsView<ListSourcePreference, String>() {
        private val _state = MutableStateFlow(
            props.currentValue ?: props.default ?: "0",
        )
        override val state: StateFlow<String> = _state.asStateFlow()

        override fun updateState(value: String) {
            _state.value = value
        }
        internal constructor(index: Int, preference: ListSourcePreference) : this(
            index,
            preference.title,
            preference.summary,
            preference,
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
        override val props: MultiSelectListSourcePreference,
    ) : SourceSettingsView<MultiSelectListSourcePreference, ImmutableList<String>?>() {
        private val _state = MutableStateFlow(
            props.currentValue?.toImmutableList() ?: props.default?.toImmutableList(),
        )
        override val state: StateFlow<ImmutableList<String>?> = _state.asStateFlow()

        override fun updateState(value: ImmutableList<String>?) {
            _state.value = value
        }
        internal constructor(index: Int, preference: MultiSelectListSourcePreference) : this(
            index,
            preference.title,
            preference.summary,
            preference,
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
        override val props: EditTextSourcePreference,
    ) : SourceSettingsView<EditTextSourcePreference, String>() {
        private val _state = MutableStateFlow(props.currentValue ?: props.default.orEmpty())
        override val state: StateFlow<String> = _state.asStateFlow()

        override fun updateState(value: String) {
            _state.value = value
        }
        internal constructor(index: Int, preference: EditTextSourcePreference) : this(
            index,
            preference.title,
            preference.summary,
            preference.dialogTitle,
            preference.dialogMessage,
            preference,
        )
    }
}

private fun withFormat(
    text: String,
    value: Any?,
): String = stringFormat(text, value)

fun SourceSettingsView(
    index: Int,
    preference: SourcePreference,
): SourceSettingsView<*, *> =
    when (preference) {
        is CheckBoxSourcePreference -> CheckBox(index, preference)
        is SwitchSourcePreference -> Switch(index, preference)
        is EditTextSourcePreference -> EditText(index, preference)
        is ListSourcePreference -> List(index, preference)
        is MultiSelectListSourcePreference -> MultiSelect(index, preference)
    }
