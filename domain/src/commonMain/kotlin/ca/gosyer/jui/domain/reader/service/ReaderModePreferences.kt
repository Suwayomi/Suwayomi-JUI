/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.reader.service

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore
import ca.gosyer.jui.domain.reader.model.DefaultReaderMode
import ca.gosyer.jui.domain.reader.model.Direction
import ca.gosyer.jui.domain.reader.model.ImageScale
import ca.gosyer.jui.domain.reader.model.NavigationMode

class ReaderModePreferences(
    private val mode: String,
    private val preferenceStore: PreferenceStore,
) {
    constructor(mode: String, factory: (String) -> PreferenceStore) :
        this(mode, factory(mode))

    private val defaultMode by lazy { DefaultReaderMode.entries.find { it.res == mode } }

    fun default(): Preference<Boolean> = preferenceStore.getBoolean("default", defaultMode != null)

    fun continuous(): Preference<Boolean> = preferenceStore.getBoolean("continuous", defaultMode?.continuous ?: false)

    fun direction(): Preference<Direction> =
        preferenceStore.getJsonObject(
            "direction",
            defaultMode?.direction ?: Direction.Down,
            Direction.serializer(),
        )

    fun padding(): Preference<Int> = preferenceStore.getInt("padding", defaultMode?.padding ?: 0)

    fun imageScale(): Preference<ImageScale> =
        preferenceStore.getJsonObject(
            "image_scale",
            defaultMode?.imageScale ?: ImageScale.FitScreen,
            ImageScale.serializer(),
        )

    fun fitSize(): Preference<Boolean> = preferenceStore.getBoolean("fit_size", false)

    fun maxSize(): Preference<Int> =
        preferenceStore.getInt(
            "max_size",
            if (defaultMode?.continuous == true) {
                if (defaultMode?.direction == Direction.Left || defaultMode?.direction == Direction.Right) {
                    500
                } else {
                    700
                }
            } else {
                0
            },
        )

    fun navigationMode(): Preference<NavigationMode> =
        preferenceStore.getJsonObject(
            "navigation",
            defaultMode?.navigationMode ?: NavigationMode.LNavigation,
            NavigationMode.serializer(),
        )
}
