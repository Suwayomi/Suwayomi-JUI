/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

val LocalDisplayController =
    compositionLocalOf<DisplayController?> { null }

class DisplayController(
    private val _sideMenuVisible: MutableState<Boolean> = mutableStateOf(true),
    private val _isDrawer: MutableState<Boolean> = mutableStateOf(false),
) {
    val sideMenuVisible by _sideMenuVisible
    val isDrawer by _isDrawer

    fun openSideMenu() {
        _sideMenuVisible.value = true
    }
    fun closeSideMenu() {
        _sideMenuVisible.value = false
    }
    fun setAsDrawer() {
        _isDrawer.value = true
    }
    fun setAsNotDrawer() {
        _isDrawer.value = false
    }
}

@Composable
fun withDisplayController(controller: DisplayController, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalDisplayController provides controller,
        content = content
    )
}
