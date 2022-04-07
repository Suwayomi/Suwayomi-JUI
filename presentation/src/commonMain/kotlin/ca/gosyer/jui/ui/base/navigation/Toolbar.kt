/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.components.keyboardHandler
import ca.gosyer.jui.uicore.resources.stringResource
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun Toolbar(
    name: String,
    navigator: Navigator? = LocalNavigator.current,
    closable: Boolean = (navigator?.size ?: 0) > 1,
    onClose: () -> Unit = { navigator?.pop() },
    modifier: Modifier = Modifier,
    actions: @Composable () -> List<ActionItem> = { emptyList() },
    backgroundColor: Color = MaterialTheme.colors.surface, // CustomColors.current.bars,
    contentColor: Color = contentColorFor(backgroundColor), // CustomColors.current.onBars,
    elevation: Dp = Dp.Hairline,
    searchText: String? = null,
    search: ((String) -> Unit)? = null,
    searchSubmit: (() -> Unit)? = null,
) {
    BoxWithConstraints {
        if (maxWidth > 600.dp) {
            WideToolbar(
                name = name,
                closable = closable,
                onClose = onClose,
                modifier = modifier,
                actions = actions,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                elevation = elevation,
                searchText = searchText,
                search = search,
                searchSubmit = searchSubmit
            )
        } else {
            ThinToolbar(
                name = name,
                closable = closable,
                onClose = onClose,
                modifier = modifier,
                actions = actions,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                elevation = elevation,
                searchText = searchText,
                search = search,
                searchSubmit = searchSubmit
            )
        }
    }
}

@Composable
private fun WideToolbar(
    name: String,
    closable: Boolean,
    onClose: () -> Unit,
    modifier: Modifier,
    actions: @Composable () -> List<ActionItem> = { emptyList() },
    backgroundColor: Color,
    contentColor: Color,
    elevation: Dp,
    searchText: String?,
    search: ((String) -> Unit)?,
    searchSubmit: (() -> Unit)?,
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
                val displayController = LocalDisplayController.current
                if (displayController != null) {
                    AnimatedVisibility(
                        !displayController.sideMenuVisible
                    ) {
                        ActionIcon(displayController::openSideMenu, "Open nav", Icons.Rounded.Sort)
                    }
                }

                Text(name, fontSize = 20.sp)
            }

            Crossfade(search != null) { showSearch ->
                if (showSearch) {
                    SearchBox(contentColor, searchText, search, searchSubmit)
                }
            }

            Row {
                ActionMenu(actions()) { onClick: () -> Unit, name: String, icon: ImageVector, enabled: Boolean ->
                    TextActionIcon(
                        onClick = onClick,
                        text = name,
                        icon = icon,
                        enabled = enabled
                    )
                }
                if (closable) {
                    TextActionIcon(
                        onClick = onClose,
                        text = stringResource(MR.strings.action_close),
                        icon = Icons.Rounded.Close
                    )
                }
            }
        }
    }
}

@Composable
private fun ThinToolbar(
    name: String,
    closable: Boolean,
    onClose: () -> Unit,
    modifier: Modifier,
    actions: @Composable () -> List<ActionItem> = { emptyList() },
    backgroundColor: Color,
    contentColor: Color,
    elevation: Dp,
    searchText: String?,
    search: ((String) -> Unit)?,
    searchSubmit: (() -> Unit)?,
) {
    var searchMode by remember { mutableStateOf(!searchText.isNullOrEmpty()) }
    fun closeSearch() {
        search?.invoke("")
        searchSubmit?.invoke()
        searchMode = false
    }
    BackHandler(searchMode, ::closeSearch)
    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        elevation = elevation,
        shape = RectangleShape,
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Row(
                Modifier.fillMaxWidth()
                    .padding(AppBarDefaults.ContentPadding)
                    .height(56.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!closable && !searchMode) {
                    Spacer(Modifier.width(12.dp))
                } else {
                    Row(Modifier.width(68.dp), verticalAlignment = Alignment.CenterVertically) {
                        CompositionLocalProvider(
                            LocalContentAlpha provides ContentAlpha.high,
                        ) {
                            IconButton(
                                onClick = {
                                    if (searchMode) {
                                        closeSearch()
                                    } else {
                                        onClose()
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.ArrowBack,
                                    stringResource(MR.strings.action_close)
                                )
                            }
                        }
                    }
                }

                if (searchMode) {
                    Row(
                        Modifier.fillMaxHeight().weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val focusManager = LocalFocusManager.current
                        BasicTextField(
                            value = searchText.orEmpty(),
                            onValueChange = search ?: {},
                            modifier = Modifier.fillMaxWidth()
                                .keyboardHandler(singleLine = true) {
                                    searchSubmit?.invoke()
                                    it.clearFocus()
                                },
                            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colors.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colors.primary),
                            keyboardActions = KeyboardActions {
                                searchSubmit?.invoke()
                                focusManager.clearFocus()
                            },
                            maxLines = 1,
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchText.isNullOrEmpty()) {
                                        Text(
                                            stringResource(MR.strings.action_searching),
                                            color = LocalTextStyle.current.color.copy(alpha = ContentAlpha.medium)
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                        )
                    }
                } else {
                    Row(
                        Modifier.fillMaxHeight().weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProvideTextStyle(value = MaterialTheme.typography.h6) {
                            CompositionLocalProvider(
                                LocalContentAlpha provides ContentAlpha.high,
                            ) {
                                Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                    Row(
                        Modifier.fillMaxHeight(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (search != null && !searchMode) {
                            IconButton(onClick = { searchMode = true }) {
                                Icon(Icons.Rounded.Search, stringResource(MR.strings.action_search))
                            }
                        }
                        ActionMenu(
                            actions(),
                            if (searchMode) {
                                1
                            } else {
                                3
                            }
                        ) { onClick: () -> Unit, name: String, icon: ImageVector, enabled: Boolean ->
                            IconButton(onClick = onClick, enabled = enabled) {
                                Icon(icon, name)
                            }
                        }
                    }
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
                modifier = Modifier.fillMaxWidth()
                    .keyboardHandler(singleLine = true) {
                        searchSubmit?.invoke()
                        it.clearFocus()
                    },
                textStyle = TextStyle(contentColor, 18.sp),
                cursorBrush = SolidColor(contentColor.copy(alpha = 0.50F)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
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
