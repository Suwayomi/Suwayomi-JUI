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

class ReaderPreferences(
    private val preferenceStore: PreferenceStore,
    private val factory: (String) -> PreferenceStore,
) {
    fun preload(): Preference<Int> = preferenceStore.getInt("preload", 3)

    fun threads(): Preference<Int> = preferenceStore.getInt("threads", 3)

    fun modes(): Preference<List<String>> =
        preferenceStore.getJsonObject(
            "modes",
            DefaultReaderMode.entries.map { it.res },
            ListSerializer(String.serializer()),
        )

    fun mode(): Preference<String> = preferenceStore.getString("mode", "RTL")

    fun getMode(mode: String): ReaderModePreferences = ReaderModePreferences(mode, factory)
}
