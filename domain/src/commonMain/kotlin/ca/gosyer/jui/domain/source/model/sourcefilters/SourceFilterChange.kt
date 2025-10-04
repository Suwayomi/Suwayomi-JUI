/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.model.sourcefilters

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SourceFilterChangeOld(
    val position: Int,
    val state: String,
) {
    constructor(position: Int, state: Any) : this(
        position,
        if (state is SortFilterOld.Selection) {
            Json.encodeToString(state)
        } else {
            state.toString()
        },
    )
}

sealed interface SourceFilter {
    val position: Int

    data class Checkbox(
        override val position: Int,
        val name: String,
        val default: Boolean,
        val value: Boolean = default,
    ) : SourceFilter

    data class Header(
        override val position: Int,
        val name: String,
    ) : SourceFilter

    data class Separator(
        override val position: Int,
        val name: String,
    ) : SourceFilter

    data class Group(
        override val position: Int,
        val name: String,
        val value: List<SourceFilter>,
    ) : SourceFilter

    data class Select(
        override val position: Int,
        val name: String,
        val values: List<String>,
        val default: Int,
        val value: Int = default,
    ) : SourceFilter

    data class Sort(
        override val position: Int,
        val name: String,
        val values: List<String>,
        val default: SelectionChange?,
        val value: SelectionChange? = default,
    ) : SourceFilter {
        data class SelectionChange(
            val ascending: Boolean,
            val index: Int,
        )
    }

    data class Text(
        override val position: Int,
        val name: String,
        val default: String,
        val value: String = default,
    ) : SourceFilter

    data class TriState(
        override val position: Int,
        val name: String,
        val default: TriStateValue,
        val value: TriStateValue = default,
    ) : SourceFilter {
        enum class TriStateValue {
            IGNORE,
            INCLUDE,
            EXCLUDE,
        }
    }
}
