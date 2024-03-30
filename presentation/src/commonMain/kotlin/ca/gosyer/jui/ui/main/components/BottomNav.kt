/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import ca.gosyer.jui.ui.main.TopLevelMenus
import ca.gosyer.jui.uicore.resources.stringResource
import cafe.adriel.voyager.navigator.Navigator

private val BottomNavHeight = 56.dp

private val BottomNavHeightLocal = compositionLocalOf { 0.dp }

val WindowInsets.Companion.bottomNav
    @Composable get() = WindowInsets(bottom = BottomNavHeightLocal.current)

@Composable
fun WithBottomNav(
    navigator: Navigator,
    content: @Composable () -> Unit,
) {
    Box {
        val isBottomNavVisible = navigator.size <= 1
        CompositionLocalProvider(
            BottomNavHeightLocal provides if (isBottomNavVisible) BottomNavHeight else 0.dp,
            content = content,
        )
        AnimatedVisibility(
            isBottomNavVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            BottomNav(navigator)
        }
    }
}

@Composable
fun BottomNav(navigator: Navigator) {
    BottomNavigation {
        TopLevelMenus.entries.fastForEach {
            val isSelected = it.isSelected(navigator)
            BottomNavigationItem(
                selected = isSelected,
                onClick = {
                    if (isSelected) return@BottomNavigationItem
                    navigator replace it.createScreen()
                },
                icon = {
                    if (isSelected) {
                        Icon(it.selectedIcon, stringResource(it.textKey))
                    } else {
                        Icon(it.unselectedIcon, stringResource(it.textKey))
                    }
                },
                label = {
                    Text(
                        text = stringResource(it.textKey),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
                modifier = Modifier
                    .windowInsetsPadding(
                        WindowInsets.navigationBars.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                        ),
                    )
                    .height(BottomNavHeight),
            )
        }
    }
}

@Composable
fun BottomNavigation(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = BottomNavigationDefaults.Elevation,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        elevation = elevation,
        modifier = modifier,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = BottomNavHeight)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceBetween,
            content = content,
        )
    }
}
