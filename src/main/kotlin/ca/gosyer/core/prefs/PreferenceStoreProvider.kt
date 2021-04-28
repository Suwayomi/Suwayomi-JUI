/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.core.prefs

import ca.gosyer.common.prefs.PreferenceStore
import com.russhwolf.settings.JvmPreferencesSettings
import java.util.prefs.Preferences

class PreferenceStoreFactory {

    fun create(vararg names: String): PreferenceStore {
        var preferences: Preferences = Preferences.userRoot()
        names.forEach {
            preferences = preferences.node(it)
        }
        return JvmPreferenceStore(JvmPreferencesSettings(preferences))
    }
}
