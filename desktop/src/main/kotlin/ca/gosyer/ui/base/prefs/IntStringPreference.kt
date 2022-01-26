/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.prefs

import ca.gosyer.core.prefs.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

fun Preference<Int>.asStringStateIn(scope: CoroutineScope): PreferenceMutableStateFlow<String> {
    return PreferenceMutableStateFlow(IntStringPreference(this), scope)
}

class IntStringPreference(private val int: Preference<Int>) : Preference<String> {
    override fun key(): String {
        return int.key()
    }

    override fun get(): String {
        return int.get().toString()
    }

    override fun set(value: String) {
        value.toIntOrNull()?.let { int.set(it) }
    }

    override fun isSet(): Boolean {
        return int.isSet()
    }

    override fun delete() {
        int.delete()
    }

    override fun defaultValue(): String {
        return int.defaultValue().toString()
    }

    override fun changes(): Flow<String> {
        return int.changes().map { it.toString() }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<String> {
        return changes().stateIn(scope, SharingStarted.Eagerly, get())
    }
}
