/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.vm

import ca.gosyer.ui.categories.CategoriesMenuViewModel
import ca.gosyer.ui.extensions.ExtensionsMenuViewModel
import ca.gosyer.ui.library.LibraryScreenViewModel
import ca.gosyer.ui.main.MainViewModel
import ca.gosyer.ui.manga.MangaMenuViewModel
import ca.gosyer.ui.sources.SourcesMenuViewModel
import ca.gosyer.ui.sources.components.SourceScreenViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { MainViewModel() }
    factory { ExtensionsMenuViewModel() }
    factory { SourcesMenuViewModel() }
    factory { SourceScreenViewModel() }
    factory { MangaMenuViewModel() }
    factory { LibraryScreenViewModel() }
    factory { CategoriesMenuViewModel() }
}
