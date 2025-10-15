/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain

import ca.gosyer.jui.core.CoreComponent
import ca.gosyer.jui.core.di.AppScope
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.domain.extension.service.ExtensionPreferences
import ca.gosyer.jui.domain.library.service.LibraryPreferences
import ca.gosyer.jui.domain.library.service.LibraryUpdateService
import ca.gosyer.jui.domain.migration.interactor.RunMigrations
import ca.gosyer.jui.domain.migration.service.MigrationPreferences
import ca.gosyer.jui.domain.reader.service.ReaderPreferences
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.HttpNoAuth
import ca.gosyer.jui.domain.server.httpClient
import ca.gosyer.jui.domain.server.httpClientNoAuth
import ca.gosyer.jui.domain.server.service.ServerHostPreferences
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.source.service.CatalogPreferences
import ca.gosyer.jui.domain.ui.service.UiPreferences
import ca.gosyer.jui.domain.updates.interactor.UpdateChecker
import ca.gosyer.jui.domain.updates.service.UpdatePreferences
import ca.gosyer.jui.domain.user.interactor.UserRefreshUI
import ca.gosyer.jui.domain.user.service.UserPreferences
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Provides

interface SharedDomainComponent : CoreComponent {
    // Factories
    val migrations: RunMigrations

    val updateChecker: UpdateChecker

    // Singletons
    val downloadService: DownloadService

    val libraryUpdateService: LibraryUpdateService

    val http: Http

    val httpNoAuth: HttpNoAuth

    val serverPreferences: ServerPreferences

    val extensionPreferences: ExtensionPreferences

    val catalogPreferences: CatalogPreferences

    val libraryPreferences: LibraryPreferences

    val readerPreferences: ReaderPreferences

    val uiPreferences: UiPreferences

    val migrationPreferences: MigrationPreferences

    val updatePreferences: UpdatePreferences

    val serverListeners: ServerListeners

    val json: Json

    val userRefreshUI: UserRefreshUI

    @get:AppScope
    @get:Provides
    val lazyUserRefreshUIFactory: Lazy<UserRefreshUI>
        get() = lazy { userRefreshUI }

    @AppScope
    @Provides
    fun httpFactory(
        serverPreferences: ServerPreferences,
        userPreferences: UserPreferences,
        userRefreshUI: Lazy<UserRefreshUI>,
        json: Json,
    ): Http = httpClient(serverPreferences, userPreferences, userRefreshUI, json)

    @AppScope
    @Provides
    fun httpNoAuthFactory(
        serverPreferences: ServerPreferences,
        userPreferences: UserPreferences,
        userRefreshUI: Lazy<UserRefreshUI>,
        json: Json,
    ): HttpNoAuth = httpClientNoAuth(serverPreferences, userPreferences, userRefreshUI, json)

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
    val userPreferencesFactory: UserPreferences
        get() = UserPreferences(preferenceFactory.create("user"))

    @get:AppScope
    @get:Provides
    val serverHostPreferencesFactory: ServerHostPreferences
        get() = ServerHostPreferences(preferenceFactory.create("host"))

    @get:AppScope
    @get:Provides
    val serverListenersFactory: ServerListeners
        get() = ServerListeners()

    @get:AppScope
    @get:Provides
    val jsonFactory: Json
        get() = Json {
            isLenient = false
            ignoreUnknownKeys = true
            allowSpecialFloatingPointValues = true
            useArrayPolymorphism = false
        }
}
