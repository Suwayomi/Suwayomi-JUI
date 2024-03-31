/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.core.lang.addSuffix
import ca.gosyer.jui.data.settings.SettingsRepositoryImpl
import ca.gosyer.jui.domain.backup.service.BackupRepositoryOld
import ca.gosyer.jui.domain.category.service.CategoryRepositoryOld
import ca.gosyer.jui.domain.chapter.service.ChapterRepositoryOld
import ca.gosyer.jui.domain.download.service.DownloadRepositoryOld
import ca.gosyer.jui.domain.extension.service.ExtensionRepositoryOld
import ca.gosyer.jui.domain.global.service.GlobalRepositoryOld
import ca.gosyer.jui.domain.library.service.LibraryRepositoryOld
import ca.gosyer.jui.domain.manga.service.MangaRepositoryOld
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.settings.service.SettingsRepository
import ca.gosyer.jui.domain.settings.service.SettingsRepositoryOld
import ca.gosyer.jui.domain.source.service.SourceRepositoryOld
import ca.gosyer.jui.domain.updates.service.UpdatesRepositoryOld
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.network.ktorClient
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import me.tatarka.inject.annotations.Provides

interface DataComponent {
    @Provides
    fun ktorfit(
        http: Http,
        serverPreferences: ServerPreferences,
    ) = Ktorfit
        .Builder()
        .httpClient(http)
        .converterFactories(FlowConverterFactory())
        .baseUrl(serverPreferences.serverUrl().get().toString().addSuffix('/'))
        .build()

    @OptIn(ApolloExperimental::class)
    @Provides
    fun apolloClient(
        http: Http,
        serverPreferences: ServerPreferences,
    ) = ApolloClient.Builder()
        .serverUrl(
            URLBuilder(serverPreferences.serverUrl().get())
                .appendPathSegments("api", "graphql")
                .buildString(),
        )
        .ktorClient(http)
        .build()

    @Provides
    fun backupRepositoryOld(ktorfit: Ktorfit) = ktorfit.create<BackupRepositoryOld>()

    @Provides
    fun categoryRepositoryOld(ktorfit: Ktorfit) = ktorfit.create<CategoryRepositoryOld>()

    @Provides
    fun chapterRepositoryOld(ktorfit: Ktorfit) = ktorfit.create<ChapterRepositoryOld>()

    @Provides
    fun downloadRepositoryOld(ktorfit: Ktorfit) = ktorfit.create<DownloadRepositoryOld>()

    @Provides
    fun extensionRepositoryOld(ktorfit: Ktorfit) = ktorfit.create<ExtensionRepositoryOld>()

    @Provides
    fun globalRepositoryOld(ktorfit: Ktorfit) = ktorfit.create<GlobalRepositoryOld>()

    @Provides
    fun libraryRepositoryOld(ktorfit: Ktorfit) = ktorfit.create<LibraryRepositoryOld>()

    @Provides
    fun mangaRepositoryOld(ktorfit: Ktorfit) = ktorfit.create<MangaRepositoryOld>()

    @Provides
    fun settingsRepositoryOld(ktorfit: Ktorfit) = ktorfit.create<SettingsRepositoryOld>()

    @Provides
    fun sourceRepositoryOld(ktorfit: Ktorfit) = ktorfit.create<SourceRepositoryOld>()

    @Provides
    fun updatesRepositoryOld(ktorfit: Ktorfit) = ktorfit.create<UpdatesRepositoryOld>()

    @Provides
    fun settingsRepository(apolloClient: ApolloClient): SettingsRepository = SettingsRepositoryImpl(apolloClient)
}
