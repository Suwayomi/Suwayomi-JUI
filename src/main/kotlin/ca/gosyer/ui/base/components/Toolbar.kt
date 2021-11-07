/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Toolbar(
    name: String,
    menuController: MenuController? = LocalMenuController.current,
    closable: Boolean,
    onClose: () -> Unit = { menuController?.backStack?.pop() },
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.surface, // CustomColors.current.bars,
    contentColor: Color = contentColorFor(backgroundColor), // CustomColors.current.onBars,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    searchText: String? = null,
    search: ((String) -> Unit)? = null,
    searchSubmit: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        elevation = elevation,
        shape = RectangleShape,
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp).height(72.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier.fillMaxHeight().animateContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (menuController != null) {
                    if (menuController.isDrawer) {
                        ActionIcon(menuController::openSideMenu, "Open nav", Icons.Rounded.Menu)
                    } else {
                        AnimatedVisibility(
                            !menuController.sideMenuVisible
                        ) {
                            ActionIcon(menuController::openSideMenu, "Open nav", Icons.Rounded.Sort)
                        }
                    }
                }

                Text(name, fontSize = 20.sp)
            }

            Crossfade(search != null) { showSearch ->
                if (showSearch) {
                    SearchBox(contentColor, searchText, search, searchSubmit)
                }
            }

            Row(Modifier.animateContentSize()) {
                actions()
                if (closable) {
                    TextActionIcon(onClick = onClose, "Close", Icons.Rounded.Close)
                }
            }
        }
    }
}

@Composable
private fun SearchBox(
    contentColor: Color,
    searchText: String?,
    search: ((String) -> Unit)?,
    searchSubmit: (() -> Unit)?
) {
    Card(
        Modifier.fillMaxHeight()
            .width(300.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(4.dp),
        elevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colors.primary)
    ) {
        Box(Modifier.fillMaxSize().padding(8.dp), Alignment.CenterStart) {
            BasicTextField(
                searchText.orEmpty(),
                onValueChange = { search?.invoke(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().then(
                    if (searchSubmit != null) {
                        Modifier.onPreviewKeyEvent { event ->
                            (event.key == Key.Enter && event.type == KeyEventType.KeyDown).also {
                                if (it) {
                                    searchSubmit()
                                }
                            }
                        }
                    } else Modifier
                ),
                textStyle = TextStyle(contentColor, 18.sp),
                cursorBrush = SolidColor(contentColor.copy(alpha = 0.50F)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
        }
    }
}

@Composable
fun ActionIcon(onClick: () -> Unit, contentDescription: String, icon: ImageVector) {
    BoxWithTooltipSurface(
        {
            Text(contentDescription, modifier = Modifier.padding(10.dp))
        }
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription)
        }
    }
}

@Composable
fun TextActionIcon(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Column(
        Modifier
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, radius = 32.dp)
            )
            .size(56.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            text,
            tint = if (enabled) {
                LocalContentColor.current
            } else {
                LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
            }
        )
        Text(
            text,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            color = if (enabled) {
                LocalContentColor.current
            } else {
                LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
            }
        )
    }
}
