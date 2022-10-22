/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import ca.gosyer.jui.core.lang.withIOContext
import dev.icerock.moko.resources.FileResource

@Composable
actual fun FileResource.rememberReadText(): String = remember {
    readText()
}

@Composable
actual fun FileResource.readTextAsync(): State<String?> = produceState<String?>(
    null,
    this.path
) {
    withIOContext {
        value = readText()
    }
}
