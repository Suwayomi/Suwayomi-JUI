/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.ui.base.components.MenuController
import ca.gosyer.ui.base.components.Toolbar
import dev.icerock.moko.resources.compose.stringResource
import ca.gosyer.i18n.MR

@Composable
fun SettingsSecurityScreen(menuController: MenuController) {
    Column {
        Toolbar(stringResource(MR.strings.settings_security_screen), menuController, true)
        Box {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
    }
}
