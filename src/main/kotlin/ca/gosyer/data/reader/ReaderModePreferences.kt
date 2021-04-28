/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.reader

import ca.gosyer.common.prefs.Preference
import ca.gosyer.common.prefs.PreferenceStore
import ca.gosyer.data.reader.model.Direction

class ReaderModePreferences(private val preferenceStore: PreferenceStore) {
    constructor(mode: String, factory: (String) -> PreferenceStore) :
        this(factory(mode))

    fun continuous(): Preference<Boolean> {
        return preferenceStore.getBoolean("continuous")
    }

    fun direction(): Preference<Direction> {
        return preferenceStore.getJsonObject("direction", Direction.Down, Direction.serializer())
    }

    fun padding(): Preference<Float> {
        return preferenceStore.getFloat("padding")
    }
}
