/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.github.zsoltk.compose.savedinstancestate.Bundle
import com.github.zsoltk.compose.savedinstancestate.LocalSavedInstanceState

const val LAZY_LIST_ITEM = "lazy_list_item"
const val LAZY_LIST_OFFSET = "lazy_list_offset"

@Composable
fun persistentLazyListState(bundle: Bundle = LocalSavedInstanceState.current): LazyListState {
    val state = rememberLazyListState(
        remember { bundle.getInt(LAZY_LIST_ITEM, 0) },
        remember { bundle.getInt(LAZY_LIST_OFFSET, 0) }
    )
    DisposableEffect(Unit) {
        onDispose {
            bundle.putInt(LAZY_LIST_ITEM, state.firstVisibleItemIndex)
            bundle.putInt(LAZY_LIST_OFFSET, state.firstVisibleItemScrollOffset)
        }
    }

    return state
}
