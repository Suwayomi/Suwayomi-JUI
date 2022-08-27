/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.compose

import androidx.compose.ui.graphics.ImageBitmap
import com.seiko.imageloader.Image
import io.ktor.client.statement.HttpResponse

expect suspend fun HttpResponse.toImageBitmap(): ImageBitmap

expect fun Image.asImageBitmap(): ImageBitmap
