/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.components

import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.onClick
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton

actual fun Modifier.sourceSideMenuItem(
    onSourceTabClick: () -> Unit,
    onSourceCloseTabClick: () -> Unit,
): Modifier =
    this
        .onClick(
            matcher = PointerMatcher.mouse(PointerButton.Primary),
            onClick = onSourceTabClick,
        )
        .onClick(
            matcher = PointerMatcher.mouse(PointerButton.Tertiary),
            onClick = onSourceCloseTabClick,
        )
