/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.model.sourcepreference

sealed interface SourcePreference {
    val position: Int
}

data class SwitchSourcePreference(
    override val position: Int,
    val key: String?,
    val title: String?,
    val summary: String?,
    val visible: Boolean,
    val enabled: Boolean,
    val currentValue: Boolean?,
    val default: Boolean,
) : SourcePreference

data class CheckBoxSourcePreference(
    override val position: Int,
    val key: String?,
    val title: String?,
    val summary: String?,
    val visible: Boolean,
    val enabled: Boolean,
    val currentValue: Boolean?,
    val default: Boolean,
) : SourcePreference

data class EditTextSourcePreference(
    override val position: Int,
    val key: String?,
    val title: String?,
    val summary: String?,
    val visible: Boolean,
    val enabled: Boolean,
    val currentValue: String?,
    val default: String?,
    val dialogTitle: String?,
    val dialogMessage: String?,
    val text: String?,
) : SourcePreference

data class ListSourcePreference(
    override val position: Int,
    val key: String?,
    val title: String?,
    val summary: String?,
    val visible: Boolean,
    val enabled: Boolean,
    val currentValue: String?,
    val default: String?,
    val entries: List<String>,
    val entryValues: List<String>,
) : SourcePreference

data class MultiSelectListSourcePreference(
    override val position: Int,
    val key: String?,
    val title: String?,
    val summary: String?,
    val visible: Boolean,
    val enabled: Boolean,
    val currentValue: List<String>?,
    val default: List<String>?,
    val dialogTitle: String?,
    val dialogMessage: String?,
    val entries: List<String>,
    val entryValues: List<String>,
) : SourcePreference
