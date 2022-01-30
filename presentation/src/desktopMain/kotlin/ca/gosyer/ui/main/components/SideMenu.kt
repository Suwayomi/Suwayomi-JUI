/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.presentation.build.BuildKonfig
import ca.gosyer.ui.base.navigation.MenuController
import ca.gosyer.ui.main.TopLevelMenus

@Composable
fun SideMenu(modifier: Modifier, controller: MenuController) {
    Surface(modifier then Modifier.fillMaxHeight(), elevation = 2.dp) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                Row(
                    Modifier.fillMaxWidth().height(60.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        BuildKonfig.NAME,
                        fontSize = 24.sp,
                        modifier = Modifier
                    )
                    IconButton(controller::closeSideMenu) {
                        Icon(Icons.Rounded.Close, contentDescription = null)
                    }
                }
                Spacer(Modifier.height(20.dp))
                remember { TopLevelMenus.values().filter(TopLevelMenus::top) }.forEach { topLevelMenu ->
                    SideMenuItem(
                        topLevelMenu.isSelected(controller.backStack),
                        topLevelMenu,
                        controller::newRoot
                    )
                }
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.align(Alignment.BottomStart).padding(bottom = 8.dp)) {
                        remember { TopLevelMenus.values().filterNot(TopLevelMenus::top) }.forEach { topLevelMenu ->
                            SideMenuItem(
                                topLevelMenu.isSelected(controller.backStack),
                                topLevelMenu,
                                controller::newRoot
                            )
                        }
                    }
                }
            }
        }
    }
}
