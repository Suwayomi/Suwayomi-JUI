/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.reader

import ca.gosyer.common.prefs.Preference
import ca.gosyer.common.prefs.PreferenceStore
import ca.gosyer.data.reader.model.DefaultReaderMode
import ca.gosyer.data.reader.model.Direction
import ca.gosyer.data.reader.model.ImageScale

class ReaderModePreferences(private val mode: String, private val preferenceStore: PreferenceStore) {
    constructor(mode: String, factory: (String) -> PreferenceStore) :
        this(mode, factory(mode))

    private val defaultMode by lazy { DefaultReaderMode.values().find { it.res == mode } }

    fun default(): Preference<Boolean> {
        return preferenceStore.getBoolean("default", defaultMode != null)
    }

    fun continuous(): Preference<Boolean> {
        return preferenceStore.getBoolean("continuous", defaultMode?.continuous ?: false)
    }

    fun direction(): Preference<Direction> {
        return preferenceStore.getJsonObject("direction", defaultMode?.direction ?: Direction.Down, Direction.serializer())
    }

    fun padding(): Preference<Float> {
        return preferenceStore.getFloat("padding", defaultMode?.padding ?: 0.0F)
    }

    fun imageScale(): Preference<ImageScale> {
        return preferenceStore.getJsonObject("direction", defaultMode?.imageScale ?: ImageScale.FitScreen, ImageScale.serializer())
    }
}
