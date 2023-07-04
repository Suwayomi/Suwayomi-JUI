/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main

import ca.gosyer.jui.domain.ui.model.StartScreen
import ca.gosyer.jui.ui.extensions.ExtensionsScreen
import ca.gosyer.jui.ui.library.LibraryScreen
import ca.gosyer.jui.ui.sources.SourcesScreen
import ca.gosyer.jui.ui.updates.UpdatesScreen

fun StartScreen.toScreen() =
    when (this) {
        StartScreen.Library -> LibraryScreen()
        StartScreen.Updates -> UpdatesScreen()
        StartScreen.Sources -> SourcesScreen()
        StartScreen.Extensions -> ExtensionsScreen()
    }

fun StartScreen.toScreenClazz() =
    when (this) {
        StartScreen.Library -> LibraryScreen::class
        StartScreen.Updates -> UpdatesScreen::class
        StartScreen.Sources -> SourcesScreen::class
        StartScreen.Extensions -> ExtensionsScreen::class
    }
