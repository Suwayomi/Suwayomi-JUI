/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.util.fastForEach
import ca.gosyer.jui.presentation.build.BuildKonfig
import ca.gosyer.jui.ui.base.navigation.DisplayController
import ca.gosyer.jui.ui.main.MoreMenus
import ca.gosyer.jui.ui.main.TopLevelMenus
import ca.gosyer.jui.uicore.insets.systemBars
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun SideMenu(modifier: Modifier, controller: DisplayController, navigator: Navigator) {
    Surface(
        Modifier.fillMaxHeight()
            .windowInsetsPadding(
                WindowInsets.systemBars.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)
            ) then modifier,
        elevation = 2.dp
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 4.dp)
        ) {
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
            remember { TopLevelMenus.values().asList().dropLast(1) }.fastForEach { topLevelMenu ->
                SideMenuItem(
                    topLevelMenu.isSelected(navigator),
                    topLevelMenu
                ) { navigator replaceAll it }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    remember { MoreMenus.values() }.forEach { topLevelMenu ->
                        SideMenuItem(
                            topLevelMenu.isSelected(navigator),
                            topLevelMenu
                        ) { navigator replaceAll it }
                    }
                }
            }
        }
    }
}
