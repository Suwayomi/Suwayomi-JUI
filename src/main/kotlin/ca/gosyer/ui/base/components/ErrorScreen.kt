/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun ErrorScreen(errorMessage: String? = null, retry: (() -> Unit)? = null) {
    Surface {
        Box(Modifier.fillMaxSize()) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                val errorFace = remember { getRandomErrorFace() }
                Text(errorFace, fontSize = 36.sp, color = MaterialTheme.colors.onBackground)
                if (errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colors.onBackground)
                }
                if (retry != null) {
                    Button(retry) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

private val ERROR_FACES = arrayOf(
    "(･o･;)",
    "Σ(ಠ_ಠ)",
    "ಥ_ಥ",
    "(˘･_･˘)",
    "(；￣Д￣)",
    "(･Д･。"
)

fun getRandomErrorFace(): String {
    return ERROR_FACES[Random.nextInt(ERROR_FACES.size)]
}
