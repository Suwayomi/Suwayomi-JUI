/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.catalog

import androidx.compose.ui.text.intl.Locale
import ca.gosyer.jui.core.lang.getDefault
import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore
import ca.gosyer.jui.data.library.model.DisplayMode

class CatalogPreferences(private val preferenceStore: PreferenceStore) {
    fun languages(): Preference<Set<String>> {
        return preferenceStore.getStringSet("enabled_langs", setOfNotNull("en", Locale.getDefault().language))
    }

    fun displayMode(): Preference<DisplayMode> {
        return preferenceStore.getJsonObject("display_mode", DisplayMode.CompactGrid, DisplayMode.serializer())
    }
}
