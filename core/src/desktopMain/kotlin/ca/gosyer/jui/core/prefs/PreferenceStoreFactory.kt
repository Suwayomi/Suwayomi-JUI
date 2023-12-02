/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.prefs

import com.russhwolf.settings.PreferencesSettings
import me.tatarka.inject.annotations.Inject
import java.util.prefs.Preferences

actual class PreferenceStoreFactory
    @Inject
    constructor() {
        private val rootNode: Preferences = Preferences.userRoot()
            .node("ca/gosyer/tachideskjui")

        actual fun create(vararg names: String): PreferenceStore =
            StandardPreferenceStore(
                PreferencesSettings(
                    rootNode.node(names.joinToString(separator = "/")),
                ),
            )
    }
