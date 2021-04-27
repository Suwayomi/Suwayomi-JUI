/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.github.zsoltk.compose.savedinstancestate.LocalSavedInstanceState

@Composable
fun <T> State<T>.persistent(key: String) {
    val bundle = LocalSavedInstanceState.current
}
