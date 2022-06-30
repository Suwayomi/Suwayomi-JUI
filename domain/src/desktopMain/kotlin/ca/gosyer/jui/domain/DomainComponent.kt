/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain

import ca.gosyer.jui.core.di.AppScope
import ca.gosyer.jui.data.DataComponent
import ca.gosyer.jui.domain.download.DownloadService
import ca.gosyer.jui.domain.library.LibraryUpdateService
import ca.gosyer.jui.domain.migration.RunMigrations
import ca.gosyer.jui.domain.server.ServerService
import ca.gosyer.jui.domain.update.UpdateChecker
import me.tatarka.inject.annotations.Provides

actual interface DomainComponent : DataComponent {

    val downloadService: DownloadService

    val libraryUpdateService: LibraryUpdateService

    val migrations: RunMigrations

    val updateChecker: UpdateChecker

    val serverService: ServerService


    @get:AppScope
    @get:Provides
    val serverServiceFactory: ServerService
        get() = ServerService(serverHostPreferences)

    @get:AppScope
    @get:Provides
    val libraryUpdateServiceFactory: LibraryUpdateService
        get() = LibraryUpdateService(serverPreferences, http)

    @get:AppScope
    @get:Provides
    val downloadServiceFactory: DownloadService
        get() = DownloadService(serverPreferences, http)

    @get:AppScope
    @get:Provides
    val migrationsFactory: RunMigrations
        get() = RunMigrations(migrationPreferences, readerPreferences)

    @get:AppScope
    @get:Provides
    val updateCheckerFactory: UpdateChecker
        get() = UpdateChecker(updatePreferences, http)
}