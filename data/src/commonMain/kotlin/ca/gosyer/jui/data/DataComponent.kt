/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.core.lang.addSuffix
import ca.gosyer.jui.domain.backup.service.BackupRepository
import ca.gosyer.jui.domain.category.service.CategoryRepository
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.download.service.DownloadRepository
import ca.gosyer.jui.domain.extension.service.ExtensionRepository
import ca.gosyer.jui.domain.global.service.GlobalRepository
import ca.gosyer.jui.domain.library.service.LibraryRepository
import ca.gosyer.jui.domain.manga.service.MangaRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.settings.service.SettingsRepository
import ca.gosyer.jui.domain.source.service.SourceRepository
import ca.gosyer.jui.domain.updates.service.UpdatesRepository
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
                .buildString()
        )
        .ktorClient(http)
        .build()

    @Provides
    fun backupRepository(ktorfit: Ktorfit) = ktorfit.create<BackupRepository>()

    @Provides
    fun categoryRepository(ktorfit: Ktorfit) = ktorfit.create<CategoryRepository>()

    @Provides
    fun chapterRepository(ktorfit: Ktorfit) = ktorfit.create<ChapterRepository>()

    @Provides
    fun downloadRepository(ktorfit: Ktorfit) = ktorfit.create<DownloadRepository>()

    @Provides
    fun extensionRepository(ktorfit: Ktorfit) = ktorfit.create<ExtensionRepository>()

    @Provides
    fun globalRepository(ktorfit: Ktorfit) = ktorfit.create<GlobalRepository>()

    @Provides
    fun libraryRepository(ktorfit: Ktorfit) = ktorfit.create<LibraryRepository>()

    @Provides
    fun mangaRepository(ktorfit: Ktorfit) = ktorfit.create<MangaRepository>()

    @Provides
    fun settingsRepository(ktorfit: Ktorfit) = ktorfit.create<SettingsRepository>()

    @Provides
    fun sourceRepository(ktorfit: Ktorfit) = ktorfit.create<SourceRepository>()

    @Provides
    fun updatesRepository(ktorfit: Ktorfit) = ktorfit.create<UpdatesRepository>()
}
