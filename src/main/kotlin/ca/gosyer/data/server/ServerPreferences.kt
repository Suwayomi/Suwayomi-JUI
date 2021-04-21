/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server

import ca.gosyer.common.prefs.Preference
import ca.gosyer.common.prefs.PreferenceStore

class ServerPreferences(private val preferenceStore: PreferenceStore) {
    fun server(): Preference<String> {
        return preferenceStore.getString("server_url", "http://localhost:4567")
    }
}