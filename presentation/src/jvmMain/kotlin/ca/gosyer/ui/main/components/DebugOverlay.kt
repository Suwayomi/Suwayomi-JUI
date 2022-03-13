/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import ca.gosyer.uicore.vm.LocalViewModelFactory

@Composable
fun DebugOverlay() {
    val vmFactory = LocalViewModelFactory.current
    val vm = remember { vmFactory.instantiate<DebugOverlayViewModel>() }
    DisposableEffect(vm) {
        onDispose(vm::onDispose)
    }
    val usedMemory by vm.usedMemoryFlow.collectAsState()
    Column {
        Text("$usedMemory/${vm.maxMemory}", color = Color.White)
    }
}
