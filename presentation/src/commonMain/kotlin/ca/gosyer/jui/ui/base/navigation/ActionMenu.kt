/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.navigation

import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.components.DropdownMenu
import ca.gosyer.jui.uicore.components.DropdownMenuItem
import ca.gosyer.jui.uicore.resources.stringResource
import kotlinx.collections.immutable.ImmutableList

// Originally from https://gist.github.com/MachFour/369ebb56a66e2f583ebfb988dda2decf

// Essentially a wrapper around a lambda function to give it a name and icon
// akin to Android menu XML entries.
// As an item on the action bar, the action will be displayed with an IconButton
// with the given icon, if not null. Otherwise, the string from the name resource is used.
// In overflow menu, item will always be displayed as text.
sealed class Action {
    abstract val name: String
    open val icon: ImageVector? = null
    open val overflowMode: OverflowMode = OverflowMode.IF_NECESSARY
    open val enabled: Boolean = true
}

data class ActionGroup(
    override val name: String,
    override val icon: ImageVector? = null,
    val actions: ImmutableList<Action>,
) : Action()

@Stable
data class ActionItem(
    override val name: String,
    override val icon: ImageVector? = null,
    override val overflowMode: OverflowMode = OverflowMode.IF_NECESSARY,
    override val enabled: Boolean = true,
    val doAction: () -> Unit,
) : Action() {
    // allow 'calling' the action like a function
    operator fun invoke() = doAction()
}

// Whether action items are allowed to overflow into a dropdown menu - or NOT SHOWN to hide
enum class OverflowMode {
    NEVER_OVERFLOW, IF_NECESSARY, ALWAYS_OVERFLOW, NOT_SHOWN
}

// Note: should be used in a RowScope
@Composable
fun ActionMenu(
    items: ImmutableList<Action>,
    numIcons: Int = 3, // includes overflow menu icon; may be overridden by NEVER_OVERFLOW
    menuVisible: MutableState<Boolean> = remember { mutableStateOf(false) },
    iconItem: @Composable (onClick: () -> Unit, name: String, icon: ImageVector, enabled: Boolean) -> Unit,
) {
    if (items.isEmpty()) {
        return
    }
    // decide how many action items to show as icons
    val (appbarActions, overflowActions) = derivedStateOf {
        separateIntoIconAndOverflow(items, numIcons)
    }.value

    var openGroup by remember { mutableStateOf<ActionGroup?>(null) }

    appbarActions.fastForEach { item ->
        key(item.hashCode()) {
            if (item.icon != null) {
                when (item) {
                    is ActionGroup -> iconItem({ openGroup = item }, item.name, item.icon!!, item.enabled)
                    is ActionItem -> iconItem(item.doAction, item.name, item.icon!!, item.enabled)
                }
            } else {
                TextButton(
                    onClick = when (item) {
                        is ActionGroup -> { { openGroup = item } }
                        is ActionItem -> item.doAction
                    },
                    enabled = item.enabled,
                ) {
                    Text(
                        text = item.name,
                        color = MaterialTheme.colors.onPrimary.copy(alpha = LocalContentAlpha.current),
                    )
                }
            }
        }
    }

    if (overflowActions.isNotEmpty()) {
        iconItem(
            { menuVisible.value = true },
            stringResource(MR.strings.action_more_actions),
            Icons.Default.MoreVert,
            true,
        )
        DropdownMenu(
            expanded = menuVisible.value,
            onDismissRequest = { menuVisible.value = false },
            offset = DpOffset(8.dp, (-56).dp),
        ) {
            overflowActions.fastForEach { item ->
                key(item.hashCode()) {
                    DropdownMenuItem(
                        onClick = {
                            menuVisible.value = false
                            when (item) {
                                is ActionGroup -> openGroup = item
                                is ActionItem -> item()
                            }
                        },
                        enabled = item.enabled,
                    ) {
                        // Icon(item.icon, item.name) just have text in the overflow menu
                        Text(item.name)
                    }
                }
            }
        }
    }
    DropdownMenu(
        openGroup != null,
        onDismissRequest = { openGroup = null },
        offset = DpOffset(8.dp, (-56).dp),
    ) {
        openGroup?.actions?.fastForEach { item ->
            key(item.hashCode()) {
                DropdownMenuItem(
                    onClick = {
                        when (item) {
                            is ActionGroup -> openGroup = item
                            is ActionItem -> {
                                openGroup = null
                                item()
                            }
                        }
                    },
                    enabled = item.enabled,
                ) {
                    // Icon(item.icon, item.name) just have text in the overflow menu
                    Text(item.name)
                }
            }
        }
    }
}

private fun separateIntoIconAndOverflow(
    items: ImmutableList<Action>,
    numIcons: Int,
): Pair<List<Action>, List<Action>> {
    var (iconCount, overflowCount, preferIconCount) = Triple(0, 0, 0)
    for (item in items) {
        when (item.overflowMode) {
            OverflowMode.NEVER_OVERFLOW -> iconCount++
            OverflowMode.IF_NECESSARY -> preferIconCount++
            OverflowMode.ALWAYS_OVERFLOW -> overflowCount++
            OverflowMode.NOT_SHOWN -> {}
        }
    }

    val needsOverflow = iconCount + preferIconCount > numIcons || overflowCount > 0
    val actionIconSpace = numIcons - (if (needsOverflow) 1 else 0)

    val iconActions = ArrayList<Action>()
    val overflowActions = ArrayList<Action>()

    var iconsAvailableBeforeOverflow = actionIconSpace - iconCount
    for (item in items) {
        when (item.overflowMode) {
            OverflowMode.NEVER_OVERFLOW -> {
                iconActions.add(item)
            }
            OverflowMode.ALWAYS_OVERFLOW -> {
                overflowActions.add(item)
            }
            OverflowMode.IF_NECESSARY -> {
                if (iconsAvailableBeforeOverflow > 0) {
                    iconActions.add(item)
                    iconsAvailableBeforeOverflow--
                } else {
                    overflowActions.add(item)
                }
            }
            OverflowMode.NOT_SHOWN -> {
                // skip
            }
        }
    }
    return iconActions to overflowActions
}
