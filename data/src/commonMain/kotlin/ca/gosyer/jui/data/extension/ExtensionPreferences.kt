/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.extension

import androidx.compose.ui.text.intl.Locale
import ca.gosyer.jui.core.lang.getDefault
import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore

class ExtensionPreferences(private val preferenceStore: PreferenceStore) {
    fun languages(): Preference<Set<String>> {
        return preferenceStore.getStringSet("enabled_langs", setOfNotNull("all", "en", Locale.getDefault().language))
    }
}
