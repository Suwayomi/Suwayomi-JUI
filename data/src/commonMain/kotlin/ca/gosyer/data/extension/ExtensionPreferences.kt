/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.extension

import ca.gosyer.core.lang.getDefault
import ca.gosyer.core.prefs.Preference
import ca.gosyer.core.prefs.PreferenceStore
import io.fluidsonic.locale.Locale

class ExtensionPreferences(private val preferenceStore: PreferenceStore) {
    fun languages(): Preference<Set<String>> {
        return preferenceStore.getStringSet("enabled_langs", setOfNotNull("all", "en", Locale.getDefault().language))
    }
}
