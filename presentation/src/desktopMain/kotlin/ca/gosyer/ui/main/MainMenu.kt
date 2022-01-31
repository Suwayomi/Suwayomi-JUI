/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DrawerValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Surface
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.ui.base.navigation.DisplayController
import ca.gosyer.ui.base.navigation.withDisplayController
import ca.gosyer.ui.main.components.SideMenu
import ca.gosyer.uicore.vm.LocalViewModelFactory
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator

const val SIDE_MENU_EXPAND_DURATION = 500

@Composable
fun MainMenu() {
    val vmFactory = LocalViewModelFactory.current
    val vm = remember { vmFactory.instantiate<MainViewModel>() }
    Surface {
        Navigator(vm.startScreen.toScreen()) { navigator ->
            val controller = remember { DisplayController() }
            BoxWithConstraints {
                // if (maxWidth > 720.dp) {
                WideMainMenu(navigator, controller)
                // } else {
                // SkinnyMainMenu(rootBundle, controller)
                // }
            }
        }
    }
}

@Composable
fun SkinnyMainMenu(
    navigator: Navigator,
    controller: DisplayController
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    LaunchedEffect(controller.sideMenuVisible) {
        if (controller.sideMenuVisible) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }
    DisposableEffect(drawerState.isOpen) {
        onDispose {
            if (drawerState.isOpen) {
                controller.openSideMenu()
            } else {
                controller.closeSideMenu()
            }
        }
    }
    DisposableEffect(Unit) {
        controller.setAsDrawer()
        onDispose {
            controller.setAsNotDrawer()
        }
    }

    ModalDrawer(
        {
            SideMenu(Modifier.fillMaxWidth(), controller, navigator)
        },
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen
    ) {
        withDisplayController(controller) {
            MainWindow(Modifier)
        }
    }
}

@Composable
fun WideMainMenu(
    navigator: Navigator,
    controller: DisplayController
) {
    Box {
        val startPadding by animateDpAsState(
            if (controller.sideMenuVisible) {
                200.dp
            } else {
                0.dp
            },
            animationSpec = tween(SIDE_MENU_EXPAND_DURATION)
        )
        if (startPadding != 0.dp) {
            SideMenu(Modifier.width(200.dp), controller, navigator)
        }
        withDisplayController(controller) {
            MainWindow(Modifier.padding(start = startPadding))
        }
    }
}

@Composable
fun MainWindow(modifier: Modifier) {
    Surface(Modifier.fillMaxSize() then modifier) {
        CurrentScreen()
    }
}
