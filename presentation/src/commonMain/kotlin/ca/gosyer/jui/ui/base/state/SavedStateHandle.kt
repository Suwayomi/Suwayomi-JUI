/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.state

import kotlinx.coroutines.flow.StateFlow

expect class SavedStateHandle {
    operator fun <T> get(key: String): T?

    operator fun <T> set(key: String, value: T?)

    fun <T> remove(key: String): T?

    fun <T> getStateFlow(key: String, initialValue: T): StateFlow<T>
}
