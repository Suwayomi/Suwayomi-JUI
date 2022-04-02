/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about.licenses.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.resources.stringResource
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.LibraryColors
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults

@Composable
expect fun getLicenses(): Libs?

@Composable
internal expect fun InternalAboutLibraries(
    libraries: List<Library>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    showAuthor: Boolean,
    showVersion: Boolean,
    showLicenseBadges: Boolean,
    colors: LibraryColors,
    itemContentPadding: PaddingValues,
    onLibraryClick: ((Library) -> Unit)?,
)

@Composable
fun AboutLibraries(
    libraries: List<Library>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showAuthor: Boolean = true,
    showVersion: Boolean = true,
    showLicenseBadges: Boolean = true,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    itemContentPadding: PaddingValues = LibraryDefaults.ContentPadding,
    onLibraryClick: ((Library) -> Unit)? = null,
) {
    InternalAboutLibraries(
        libraries = libraries,
        modifier = modifier,
        contentPadding = contentPadding,
        showAuthor = showAuthor,
        showVersion = showVersion,
        showLicenseBadges = showLicenseBadges,
        colors = colors,
        itemContentPadding = itemContentPadding,
        onLibraryClick = onLibraryClick
    )
}

// TODO: 2022-04-02 Add scrollbar
@Composable
fun LicensesContent() {
    Scaffold(
        topBar = {
            Toolbar(stringResource(MR.strings.open_source_licenses))
        }
    ) {
        Box(Modifier.fillMaxSize().padding(it)) {
            val libs = getLicenses()
            if (libs != null) {
                AboutLibraries(
                    libraries = libs.libraries
                )
            } else {
                LoadingScreen()
            }
        }
    }
}
