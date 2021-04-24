/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.ui.main.Routing
import com.github.zsoltk.compose.router.BackStack
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.WindowClose

@Composable
fun Toolbar(
    name: String,
    router: BackStack<Routing>? = null,
    closable: Boolean,
    search: ((String) -> Unit)? = null
) {
    val searchText = remember { mutableStateOf("") }
    Surface(Modifier.fillMaxWidth().height(32.dp), elevation = 2.dp) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontSize = 24.sp)
            if (search != null) {
                BasicTextField(
                    searchText.value,
                    onValueChange = {
                        searchText.value = it
                        search(it)
                    }
                )
            }
            if (closable) {
                IconButton(
                    onClick = {
                        router?.pop()
                    }
                ) {
                    Icon(FontAwesomeIcons.Regular.WindowClose, "close", Modifier.size(32.dp))
                }
            }
        }
    }
}