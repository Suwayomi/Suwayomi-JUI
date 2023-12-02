/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.library.service

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore
import ca.gosyer.jui.domain.library.model.FilterState
import ca.gosyer.jui.domain.library.model.Sort

class LibraryPreferences(
    private val preferenceStore: PreferenceStore,
) {
    fun showAllCategory(): Preference<Boolean> = preferenceStore.getBoolean("show_all_category", false)

    fun filterDownloaded(): Preference<FilterState> =
        preferenceStore.getJsonObject(
            "filter_downloaded",
            FilterState.IGNORED,
            FilterState.serializer(),
        )

    fun filterUnread(): Preference<FilterState> =
        preferenceStore.getJsonObject(
            "filter_unread",
            FilterState.IGNORED,
            FilterState.serializer(),
        )

    fun filterCompleted(): Preference<FilterState> =
        preferenceStore.getJsonObject(
            "filter_completed",
            FilterState.IGNORED,
            FilterState.serializer(),
        )

    fun sortMode(): Preference<Sort> = preferenceStore.getJsonObject("sort_mode", Sort.ALPHABETICAL, Sort.serializer())

    fun sortAscending(): Preference<Boolean> = preferenceStore.getBoolean("sort_ascending", true)

    fun displayMode(): Preference<ca.gosyer.jui.domain.library.model.DisplayMode> =
        preferenceStore.getJsonObject(
            "display_mode",
            ca.gosyer.jui.domain.library.model.DisplayMode.CompactGrid,
            ca.gosyer.jui.domain.library.model.DisplayMode.serializer(),
        )

    fun gridColumns(): Preference<Int> = preferenceStore.getInt("grid_columns", 0)

    fun gridSize(): Preference<Int> = preferenceStore.getInt("grid_size", 160)

    fun unreadBadge(): Preference<Boolean> = preferenceStore.getBoolean("unread_badge", true)

    fun downloadBadge(): Preference<Boolean> = preferenceStore.getBoolean("download_badge", false)

    fun languageBadge(): Preference<Boolean> = preferenceStore.getBoolean("language_badge", false)

    fun localBadge(): Preference<Boolean> = preferenceStore.getBoolean("local_badge", false)
}
