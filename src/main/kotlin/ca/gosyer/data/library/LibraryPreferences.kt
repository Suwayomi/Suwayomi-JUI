/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.library

import ca.gosyer.common.prefs.Preference
import ca.gosyer.common.prefs.PreferenceStore
import ca.gosyer.data.library.model.DisplayMode

class LibraryPreferences(private val preferenceStore: PreferenceStore) {

    fun displayMode(): Preference<DisplayMode> {
        return preferenceStore.getJsonObject("display_mode", DisplayMode.CompactGrid, DisplayMode.serializer())
    }
}