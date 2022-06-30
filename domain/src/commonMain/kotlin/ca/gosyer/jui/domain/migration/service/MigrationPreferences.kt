/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.migration.service

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore

class MigrationPreferences(private val preferenceStore: PreferenceStore) {
    fun version(): Preference<Int> {
        return preferenceStore.getInt("version", 0)
    }
    fun appVersion(): Preference<Int> {
        return preferenceStore.getInt("app_version", 0)
    }
}
