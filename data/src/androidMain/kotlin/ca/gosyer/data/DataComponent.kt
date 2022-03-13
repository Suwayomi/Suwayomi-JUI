/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data

import android.content.Context
import ca.gosyer.core.di.AppScope
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
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.update.UpdateChecker
import ca.gosyer.data.update.UpdatePreferences
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@AppScope
@Component
actual abstract class DataComponent(
    @get:AppScope
    @get:Provides
    val context: Context
) {
    protected abstract val preferenceFactory: PreferenceStoreFactory

    protected abstract val httpProvider: HttpProvider

    abstract val downloadService: DownloadService

    abstract val libraryUpdateService: LibraryUpdateService

    abstract val migrations: Migrations

    abstract val updateChecker: UpdateChecker

    abstract val http: Http

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
