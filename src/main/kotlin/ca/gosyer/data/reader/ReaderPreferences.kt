/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.reader

import ca.gosyer.common.prefs.Preference
import ca.gosyer.common.prefs.PreferenceStore
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

class ReaderPreferences(private val preferenceStore: PreferenceStore, val factory: (String) -> PreferenceStore) {

    fun modes(): Preference<List<String>> {
        return preferenceStore.getJsonObject(
            "modes",
            listOf("RTL", "LTR", "Vertical", "Continues Vertical", "Long Strip"),
            ListSerializer(String.serializer())
        )
    }

    fun mode(): Preference<String> {
        return preferenceStore.getString("mode", "RTL")
    }

    fun getMode(mode: String): ReaderModePreferences {
        return ReaderModePreferences(mode, factory)
    }
}
