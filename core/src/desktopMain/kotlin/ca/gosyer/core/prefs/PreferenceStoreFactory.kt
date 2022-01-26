/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.core.prefs

import com.russhwolf.settings.JvmPreferencesSettings
import java.util.prefs.Preferences

class PreferenceStoreFactory {
    private val rootNode: Preferences = Preferences.userRoot()
        .node("ca/gosyer/tachideskjui")

    fun create(vararg names: String): PreferenceStore {
        return JvmPreferenceStore(
            JvmPreferencesSettings(
                rootNode.node(names.joinToString(separator = "/"))
            )
        )
    }
}
