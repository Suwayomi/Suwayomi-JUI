/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.uicore.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.PopupProperties as AndroidPopupProperties
import androidx.compose.ui.window.SecureFlagPolicy as AndroidSecureFlagPolicy

@Composable
internal actual fun RealDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    offset: DpOffset,
    properties: PopupProperties,
    content: @Composable ColumnScope.() -> Unit
) = androidx.compose.material.DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    properties = properties.toAndroidProperties(),
    modifier = modifier,
    offset = offset,
    content = content
)

@Composable
internal actual fun RealDropdownMenuItem(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource,
    content: @Composable RowScope.() -> Unit
) = androidx.compose.material.DropdownMenuItem(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@Stable
fun PopupProperties.toAndroidProperties() = AndroidPopupProperties(
    focusable = focusable,
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    securePolicy = securePolicy.toAndroidSecureFlagPolicy(),
    excludeFromSystemGesture = excludeFromSystemGesture,
    clippingEnabled = clippingEnabled,
    usePlatformDefaultWidth = usePlatformDefaultWidth
)

@Stable
fun SecureFlagPolicy.toAndroidSecureFlagPolicy() = when (this) {
    SecureFlagPolicy.Inherit -> AndroidSecureFlagPolicy.Inherit
    SecureFlagPolicy.SecureOn -> AndroidSecureFlagPolicy.SecureOn
    SecureFlagPolicy.SecureOff -> AndroidSecureFlagPolicy.SecureOff
}
