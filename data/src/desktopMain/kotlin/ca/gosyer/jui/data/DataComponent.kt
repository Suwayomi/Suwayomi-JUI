/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.core.di.AppScope
import ca.gosyer.jui.core.prefs.PreferenceStoreFactory
import ca.gosyer.jui.data.catalog.CatalogPreferences
import ca.gosyer.jui.data.download.DownloadService
import ca.gosyer.jui.data.extension.ExtensionPreferences
import ca.gosyer.jui.data.library.LibraryPreferences
import ca.gosyer.jui.data.library.LibraryUpdateService
import ca.gosyer.jui.data.migration.MigrationPreferences
import ca.gosyer.jui.data.migration.Migrations
import ca.gosyer.jui.data.reader.ReaderPreferences
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.HttpProvider
import ca.gosyer.jui.data.server.ServerHostPreferences
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.ServerService
import ca.gosyer.jui.data.ui.UiPreferences
import ca.gosyer.jui.data.update.UpdateChecker
import ca.gosyer.jui.data.update.UpdatePreferences
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@AppScope
@Component
actual abstract class DataComponent {
    protected abstract val preferenceFactory: PreferenceStoreFactory

    protected abstract val httpProvider: HttpProvider

    abstract val downloadService: DownloadService

    abstract val libraryUpdateService: LibraryUpdateService

    abstract val migrations: Migrations

    abstract val updateChecker: UpdateChecker

    abstract val serverService: ServerService

    abstract val http: Http

    abstract val serverHostPreferences: ServerHostPreferences

    abstract val serverPreferences: ServerPreferences

    abstract val extensionPreferences: ExtensionPreferences

    abstract val catalogPreferences: CatalogPreferences

    abstract val libraryPreferences: LibraryPreferences

    abstract val readerPreferences: ReaderPreferences

    abstract val uiPreferences: UiPreferences

    abstract val migrationPreferences: MigrationPreferences

    abstract val updatePreferences: UpdatePreferences

    @get:AppScope
    @get:Provides
    protected val serverHostPreferencesFactory: ServerHostPreferences
        get() = ServerHostPreferences(preferenceFactory.create("host"))

    @get:AppScope
    @get:Provides
    protected val serverPreferencesFactory: ServerPreferences
        get() = ServerPreferences(preferenceFactory.create("server"))

    @get:AppScope
    @get:Provides
    protected val extensionPreferencesFactory: ExtensionPreferences
        get() = ExtensionPreferences(preferenceFactory.create("extension"))

    @get:AppScope
    @get:Provides
    protected val catalogPreferencesFactory: CatalogPreferences
        get() = CatalogPreferences(preferenceFactory.create("catalog"))

    @get:AppScope
    @get:Provides
    protected val libraryPreferencesFactory: LibraryPreferences
        get() = LibraryPreferences(preferenceFactory.create("library"))

    @get:AppScope
    @get:Provides
    protected val readerPreferencesFactory: ReaderPreferences
        get() = ReaderPreferences(preferenceFactory.create("reader")) { name ->
            preferenceFactory.create("reader", name)
        }

    @get:AppScope
    @get:Provides
    protected val uiPreferencesFactory: UiPreferences
        get() = UiPreferences(preferenceFactory.create("ui"))

    @get:AppScope
    @get:Provides
    protected val migrationPreferencesFactory: MigrationPreferences
        get() = MigrationPreferences(preferenceFactory.create("migration"))

    @get:AppScope
    @get:Provides
    protected val updatePreferencesFactory: UpdatePreferences
        get() = UpdatePreferences(preferenceFactory.create("update"))

    @get:AppScope
    @get:Provides
    protected val httpFactory: Http
        get() = httpProvider.get(serverPreferences)

    @get:AppScope
    @get:Provides
    protected val serverServiceFactory: ServerService
        get() = ServerService(serverHostPreferences)

    @get:AppScope
    @get:Provides
    protected val libraryUpdateServiceFactory: LibraryUpdateService
        get() = LibraryUpdateService(serverPreferences, http)

    @get:AppScope
    @get:Provides
    protected val downloadServiceFactory: DownloadService
        get() = DownloadService(serverPreferences, http)

    @get:AppScope
    @get:Provides
    protected val migrationsFactory: Migrations
        get() = Migrations(migrationPreferences, readerPreferences)

    @get:AppScope
    @get:Provides
    protected val updateCheckerFactory: UpdateChecker
        get() = UpdateChecker(updatePreferences, http)

    companion object
}
