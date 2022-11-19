/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about.licenses.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ca.gosyer.jui.core.lang.withIOContext
import ca.gosyer.jui.i18n.MR
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.Libraries
import com.mikepenz.aboutlibraries.ui.compose.LibraryColors
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import kotlinx.collections.immutable.ImmutableList

@Composable
actual fun getLicenses(): Libs? {
    val libs by produceState<Libs?>(
        null
    ) {
        withIOContext {
            val json = MR.files.aboutlibraries.readText()
            value = Libs.Builder().withJson(json).build()
        }
    }
    return libs
}

actual val LibraryDefaultsContentPadding
    get() = LibraryDefaults.ContentPadding

@Composable
actual fun RealLibraryDefaultsLibraryColors(
    backgroundColor: Color,
    contentColor: Color,
    badgeBackgroundColor: Color,
    badgeContentColor: Color
) = LibraryDefaults.libraryColors(backgroundColor, contentColor, badgeBackgroundColor, badgeContentColor)

actual typealias LibraryColors = LibraryColors

@Composable
actual fun InternalAboutLibraries(
    libraries: ImmutableList<Library>,
    modifier: Modifier,
    lazyListState: LazyListState,
    contentPadding: PaddingValues,
    showAuthor: Boolean,
    showVersion: Boolean,
    showLicenseBadges: Boolean,
    colors: LibraryColors,
    itemContentPadding: PaddingValues,
    onLibraryClick: ((Library) -> Unit)?
) {
    Libraries(
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
