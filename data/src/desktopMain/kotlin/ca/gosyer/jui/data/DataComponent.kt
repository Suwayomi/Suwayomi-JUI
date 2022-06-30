/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.core.di.AppScope
import ca.gosyer.jui.core.prefs.PreferenceStoreFactory
import ca.gosyer.jui.data.catalog.CatalogPreferences
import ca.gosyer.jui.data.extension.ExtensionPreferences
import ca.gosyer.jui.data.library.LibraryPreferences
import ca.gosyer.jui.data.migration.MigrationPreferences
import ca.gosyer.jui.data.reader.ReaderPreferences
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.HttpProvider
import ca.gosyer.jui.data.server.ServerHostPreferences
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.ui.UiPreferences
import ca.gosyer.jui.data.update.UpdatePreferences
import me.tatarka.inject.annotations.Provides

actual interface DataComponent {
    val preferenceFactory: PreferenceStoreFactory

    val httpProvider: HttpProvider

    val http: Http

    val serverHostPreferences: ServerHostPreferences

    val serverPreferences: ServerPreferences

    val extensionPreferences: ExtensionPreferences

    val catalogPreferences: CatalogPreferences

    val libraryPreferences: LibraryPreferences

    val readerPreferences: ReaderPreferences

    val uiPreferences: UiPreferences

    val migrationPreferences: MigrationPreferences

    val updatePreferences: UpdatePreferences

    @get:AppScope
    @get:Provides
    val serverHostPreferencesFactory: ServerHostPreferences
        get() = ServerHostPreferences(preferenceFactory.create("host"))

    @get:AppScope
    @get:Provides
    val serverPreferencesFactory: ServerPreferences
        get() = ServerPreferences(preferenceFactory.create("server"))

    @get:AppScope
    @get:Provides
    val extensionPreferencesFactory: ExtensionPreferences
        get() = ExtensionPreferences(preferenceFactory.create("extension"))

    @get:AppScope
    @get:Provides
    val catalogPreferencesFactory: CatalogPreferences
        get() = CatalogPreferences(preferenceFactory.create("catalog"))

    @get:AppScope
    @get:Provides
    val libraryPreferencesFactory: LibraryPreferences
        get() = LibraryPreferences(preferenceFactory.create("library"))

    @get:AppScope
    @get:Provides
    val readerPreferencesFactory: ReaderPreferences
        get() = ReaderPreferences(preferenceFactory.create("reader")) { name ->
            preferenceFactory.create("reader", name)
        }

    @get:AppScope
    @get:Provides
    val uiPreferencesFactory: UiPreferences
        get() = UiPreferences(preferenceFactory.create("ui"))

    @get:AppScope
    @get:Provides
    val migrationPreferencesFactory: MigrationPreferences
        get() = MigrationPreferences(preferenceFactory.create("migration"))

    @get:AppScope
    @get:Provides
    val updatePreferencesFactory: UpdatePreferences
        get() = UpdatePreferences(preferenceFactory.create("update"))

    @get:AppScope
    @get:Provides
    val httpFactory: Http
        get() = httpProvider.get(serverPreferences)

    companion object
}
