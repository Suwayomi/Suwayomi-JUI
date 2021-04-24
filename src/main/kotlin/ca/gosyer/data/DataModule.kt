/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data

import ca.gosyer.core.prefs.PreferenceStoreFactory
import ca.gosyer.data.catalog.CatalogPreferences
import ca.gosyer.data.extension.ExtensionPreferences
import ca.gosyer.data.library.LibraryPreferences
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.HttpProvider
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.interactions.CategoryInteractionHandler
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.data.server.interactions.ExtensionInteractionHandler
import ca.gosyer.data.server.interactions.LibraryInteractionHandler
import ca.gosyer.data.server.interactions.MangaInteractionHandler
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.data.ui.UiPreferences
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module

@Suppress("FunctionName")
val DataModule = module {
    val preferenceFactory = PreferenceStoreFactory()

    bind<ServerPreferences>()
        .toProviderInstance { ServerPreferences(preferenceFactory.create("server")) }
        .providesSingleton()

    bind<ExtensionPreferences>()
        .toProviderInstance { ExtensionPreferences(preferenceFactory.create("extension")) }
        .providesSingleton()

    bind<CatalogPreferences>()
        .toProviderInstance { CatalogPreferences(preferenceFactory.create("catalog")) }
        .providesSingleton()

    bind<LibraryPreferences>()
        .toProviderInstance { LibraryPreferences(preferenceFactory.create("library")) }
        .providesSingleton()

    bind<UiPreferences>()
        .toProviderInstance { UiPreferences(preferenceFactory.create("ui")) }
        .providesSingleton()

    bind<Http>()
        .toProvider(HttpProvider::class)
        .providesSingleton()

    bind<CategoryInteractionHandler>()
        .toClass<CategoryInteractionHandler>()
    bind<ChapterInteractionHandler>()
        .toClass<ChapterInteractionHandler>()
    bind<ExtensionInteractionHandler>()
        .toClass<ExtensionInteractionHandler>()
    bind<LibraryInteractionHandler>()
        .toClass<LibraryInteractionHandler>()
    bind<MangaInteractionHandler>()
        .toClass<MangaInteractionHandler>()
    bind<SourceInteractionHandler>()
        .toClass<SourceInteractionHandler>()
}
