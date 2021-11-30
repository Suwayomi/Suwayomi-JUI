/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data

import ca.gosyer.core.prefs.PreferenceStoreFactory
import ca.gosyer.data.catalog.CatalogPreferences
import ca.gosyer.data.download.DownloadService
import ca.gosyer.data.extension.ExtensionPreferences
import ca.gosyer.data.library.LibraryPreferences
import ca.gosyer.data.library.LibraryUpdateService
import ca.gosyer.data.migration.MigrationPreferences
import ca.gosyer.data.migration.Migrations
import ca.gosyer.data.reader.ReaderPreferences
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.HttpProvider
import ca.gosyer.data.server.KamelConfigProvider
import ca.gosyer.data.server.ServerHostPreferences
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.ServerService
import ca.gosyer.data.server.interactions.BackupInteractionHandler
import ca.gosyer.data.server.interactions.CategoryInteractionHandler
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.data.server.interactions.DownloadInteractionHandler
import ca.gosyer.data.server.interactions.ExtensionInteractionHandler
import ca.gosyer.data.server.interactions.LibraryInteractionHandler
import ca.gosyer.data.server.interactions.MangaInteractionHandler
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.data.translation.ResourceProvider
import ca.gosyer.data.translation.XmlResourceBundle
import ca.gosyer.data.ui.UiPreferences
import io.kamel.core.config.KamelConfig
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module

@Suppress("FunctionName")
val DataModule = module {
    val preferenceFactory = PreferenceStoreFactory()

    bind<ServerPreferences>()
        .toProviderInstance { ServerPreferences(preferenceFactory.create("server")) }
        .providesSingleton()
    bind<ServerHostPreferences>()
        .toProviderInstance { ServerHostPreferences(preferenceFactory.create("host")) }
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

    bind<ReaderPreferences>()
        .toProviderInstance { ReaderPreferences(preferenceFactory.create("reader")) { name -> preferenceFactory.create("reader", name) } }
        .providesSingleton()

    bind<UiPreferences>()
        .toProviderInstance { UiPreferences(preferenceFactory.create("ui")) }
        .providesSingleton()

    bind<MigrationPreferences>()
        .toProviderInstance { MigrationPreferences(preferenceFactory.create("migration")) }
        .providesSingleton()

    bind<Http>()
        .toProvider(HttpProvider::class)
        .providesSingleton()

    bind<KamelConfig>()
        .toProvider(KamelConfigProvider::class)
        .providesSingleton()

    bind<XmlResourceBundle>()
        .toProvider(ResourceProvider::class)
        .providesSingleton()

    bind<BackupInteractionHandler>()
        .toClass<BackupInteractionHandler>()
    bind<CategoryInteractionHandler>()
        .toClass<CategoryInteractionHandler>()
    bind<ChapterInteractionHandler>()
        .toClass<ChapterInteractionHandler>()
    bind<DownloadInteractionHandler>()
        .toClass<DownloadInteractionHandler>()
    bind<ExtensionInteractionHandler>()
        .toClass<ExtensionInteractionHandler>()
    bind<LibraryInteractionHandler>()
        .toClass<LibraryInteractionHandler>()
    bind<MangaInteractionHandler>()
        .toClass<MangaInteractionHandler>()
    bind<SourceInteractionHandler>()
        .toClass<SourceInteractionHandler>()

    bind<ServerService>()
        .toClass<ServerService>()
        .singleton()

    bind<DownloadService>()
        .toClass<DownloadService>()
        .singleton()
    bind<LibraryUpdateService>()
        .toClass<LibraryUpdateService>()
        .singleton()

    bind<Migrations>()
        .toClass<Migrations>()
        .singleton()
}
