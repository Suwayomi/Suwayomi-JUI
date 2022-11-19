/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about.licenses.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.insets.navigationBars
import ca.gosyer.jui.uicore.insets.statusBars
import ca.gosyer.jui.uicore.resources.stringResource
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
expect fun getLicenses(): Libs?

@Composable
internal expect fun InternalAboutLibraries(
    libraries: ImmutableList<Library>,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    contentPadding: PaddingValues,
    showAuthor: Boolean,
    showVersion: Boolean,
    showLicenseBadges: Boolean,
    colors: LibraryColors,
    itemContentPadding: PaddingValues,
    onLibraryClick: ((Library) -> Unit)?
)

expect val LibraryDefaultsContentPadding: PaddingValues

@Composable
fun LibraryDefaultsLibraryColors(
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = contentColorFor(backgroundColor),
    badgeBackgroundColor: Color = MaterialTheme.colors.primary,
    badgeContentColor: Color = contentColorFor(badgeBackgroundColor)
) = RealLibraryDefaultsLibraryColors(
    backgroundColor = backgroundColor,
    contentColor = contentColor,
    badgeBackgroundColor = badgeBackgroundColor,
    badgeContentColor = badgeContentColor
)

@Composable
internal expect fun RealLibraryDefaultsLibraryColors(
    backgroundColor: Color,
    contentColor: Color,
    badgeBackgroundColor: Color,
    badgeContentColor: Color
): LibraryColors

expect interface LibraryColors

@Composable
fun AboutLibraries(
    libraries: ImmutableList<Library>,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showAuthor: Boolean = true,
    showVersion: Boolean = true,
    showLicenseBadges: Boolean = true,
    colors: LibraryColors = LibraryDefaultsLibraryColors(),
    itemContentPadding: PaddingValues = LibraryDefaultsContentPadding,
    onLibraryClick: ((Library) -> Unit)? = null
) {
    InternalAboutLibraries(
        libraries = libraries,
        modifier = modifier,
        lazyListState = lazyListState,
        contentPadding = contentPadding,
        showAuthor = showAuthor,
        showVersion = showVersion,
        showLicenseBadges = showLicenseBadges,
        colors = colors,
        itemContentPadding = itemContentPadding,
        onLibraryClick = onLibraryClick
    )
}

@Composable
fun LicensesContent() {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
            )
        ),
        topBar = {
            Toolbar(stringResource(MR.strings.open_source_licenses))
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val libs = getLicenses()
            if (libs != null) {
                val state = rememberLazyListState()
                val uriHandler = LocalUriHandler.current
                AboutLibraries(
                    libraries = remember(libs) { libs.libraries.toImmutableList() },
                    lazyListState = state,
                    onLibraryClick = {
                        it.website?.let(uriHandler::openUri)
                    },
                    contentPadding = WindowInsets.bottomNav.add(
                        WindowInsets.navigationBars.only(
                            WindowInsetsSides.Bottom
                        )
                    ).asPaddingValues()
                )
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .scrollbarPadding()
                        .windowInsetsPadding(
                            WindowInsets.bottomNav.add(
                                WindowInsets.navigationBars.only(
                                    WindowInsetsSides.Bottom
                                )
                            )
                        ),
                    adapter = rememberScrollbarAdapter(state)
                )
            } else {
                LoadingScreen()
            }
        }
    }
}
