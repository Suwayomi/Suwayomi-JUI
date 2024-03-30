/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.server.service

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class ServerHostPreferences actual constructor(
    @Suppress("unused") private val preferenceStore: PreferenceStore,
) {
    actual fun host(): Preference<Boolean> =
        object : Preference<Boolean> {
            override fun key(): String = "host"

            override fun get(): Boolean = false

            override fun isSet(): Boolean = false

            override fun delete() {}

            override fun defaultValue(): Boolean = false

            override fun changes(): Flow<Boolean> = MutableStateFlow(false)

            override fun stateIn(scope: CoroutineScope): StateFlow<Boolean> = MutableStateFlow(false)

            override fun set(value: Boolean) {}
        }
}
