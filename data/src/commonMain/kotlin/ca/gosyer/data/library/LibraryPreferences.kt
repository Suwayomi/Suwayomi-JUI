/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.library

import ca.gosyer.core.prefs.Preference
import ca.gosyer.core.prefs.PreferenceStore
import ca.gosyer.data.library.model.DisplayMode
import ca.gosyer.data.library.model.Sort

class LibraryPreferences(private val preferenceStore: PreferenceStore) {

    fun displayMode(): Preference<DisplayMode> {
        return preferenceStore.getJsonObject("display_mode", DisplayMode.CompactGrid, DisplayMode.serializer())
    }

    fun sortMode(): Preference<Sort> {
        return preferenceStore.getJsonObject("sort_mode", Sort.ALPHABETICAL, Sort.serializer())
    }

    fun sortAscending(): Preference<Boolean> {
        return preferenceStore.getBoolean("sort_ascending", true)
    }

    fun gridColumns(): Preference<Int> {
        return preferenceStore.getInt("grid_columns", 0)
    }

    fun gridSize(): Preference<Int> {
        return preferenceStore.getInt("grid_size", 160)
    }

    fun showAllCategory(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_all_category", false)
    }
}
