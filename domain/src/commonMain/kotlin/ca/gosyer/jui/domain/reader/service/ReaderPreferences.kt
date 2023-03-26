/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.reader.service

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore
import ca.gosyer.jui.domain.reader.model.DefaultReaderMode
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

class ReaderPreferences(private val preferenceStore: PreferenceStore, private val factory: (String) -> PreferenceStore) {

    fun preload(): Preference<Int> {
        return preferenceStore.getInt("preload", 3)
    }

    fun threads(): Preference<Int> {
        return preferenceStore.getInt("threads", 3)
    }

    fun modes(): Preference<List<String>> {
        return preferenceStore.getJsonObject(
            "modes",
            DefaultReaderMode.values().map { it.res },
            ListSerializer(String.serializer()),
        )
    }

    fun mode(): Preference<String> {
        return preferenceStore.getString("mode", "RTL")
    }

    fun getMode(mode: String): ReaderModePreferences {
        return ReaderModePreferences(mode, factory)
    }
}
