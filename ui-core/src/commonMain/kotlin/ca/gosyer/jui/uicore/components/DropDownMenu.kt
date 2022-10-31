/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * Policy on setting [WindowManager.LayoutParams.FLAG_SECURE] on a window.
 */
@Stable
enum class SecureFlagPolicy {
    /**
     * Inherit [WindowManager.LayoutParams.FLAG_SECURE] from the parent window and pass it on the
     * window that is using this policy.
     */
    Inherit,

    /**
     * Forces [WindowManager.LayoutParams.FLAG_SECURE] to be set on the window that is using this
     * policy.
     */
    SecureOn,

    /**
     * No [WindowManager.LayoutParams.FLAG_SECURE] will be set on the window that is using this
     * policy.
     */
    SecureOff
}

@Immutable
data class PopupProperties @ExperimentalComposeUiApi constructor(
    val focusable: Boolean = false,
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
    val excludeFromSystemGesture: Boolean = true,
    val clippingEnabled: Boolean = true,
    @property:ExperimentalComposeUiApi
    val usePlatformDefaultWidth: Boolean = false
)

@Composable
internal expect fun RealDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    offset: DpOffset,
    properties: PopupProperties,
    content: @Composable ColumnScope.() -> Unit
)

@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit
) = RealDropdownMenu(expanded, onDismissRequest, modifier, offset, properties, content)

@Composable
internal expect fun RealDropdownMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource,
    content: @Composable RowScope.() -> Unit
)

@Composable
fun DropdownMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) = RealDropdownMenuItem(onClick, modifier, enabled, contentPadding, interactionSource, content)
