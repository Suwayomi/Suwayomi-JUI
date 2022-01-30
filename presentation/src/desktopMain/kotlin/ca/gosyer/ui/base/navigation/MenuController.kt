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
import ca.gosyer.ui.main.Routes
import com.github.zsoltk.compose.router.BackStack

val LocalMenuController =
    compositionLocalOf<MenuController?> { null }

class MenuController(
    val backStack: BackStack<Routes>,
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

    fun push(route: Routes) {
        backStack.push(route)
        if (isDrawer) {
            closeSideMenu()
        }
    }
    fun newRoot(route: Routes) {
        backStack.newRoot(route)
        if (isDrawer) {
            closeSideMenu()
        }
    }
}

@Composable
fun withMenuController(controller: MenuController, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalMenuController provides controller,
        content = content
    )
}
