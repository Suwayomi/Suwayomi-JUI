/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.catalog

import ca.gosyer.common.prefs.Preference
import ca.gosyer.common.prefs.PreferenceStore

class CatalogPreferences(private val preferenceStore: PreferenceStore) {
    fun languages(): Preference<Set<String>> {
        return preferenceStore.getStringSet("enabled_langs", setOf("en"))
    }
}
