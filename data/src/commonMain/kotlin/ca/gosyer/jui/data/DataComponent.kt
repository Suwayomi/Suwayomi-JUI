/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.data.backup.BackupRepositoryImpl
import ca.gosyer.jui.data.category.CategoryRepositoryImpl
import ca.gosyer.jui.data.chapter.ChapterRepositoryImpl
import ca.gosyer.jui.data.download.DownloadRepositoryImpl
import ca.gosyer.jui.data.extension.ExtensionRepositoryImpl
import ca.gosyer.jui.data.global.GlobalRepositoryImpl
import ca.gosyer.jui.data.library.LibraryRepositoryImpl
import ca.gosyer.jui.data.manga.MangaRepositoryImpl
import ca.gosyer.jui.data.settings.SettingsRepositoryImpl
import ca.gosyer.jui.data.source.SourceRepositoryImpl
import ca.gosyer.jui.data.updates.UpdatesRepositoryImpl
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
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.ws.GraphQLWsProtocol
import com.apollographql.ktor.ktorClient
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import me.tatarka.inject.annotations.Provides

interface DataComponent : SharedDataComponent {
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
        .wsProtocol(GraphQLWsProtocol.Factory())
        .dispatcher(Dispatchers.IO)
        .build()

    @Provides
    fun settingsRepository(apolloClient: ApolloClient): SettingsRepository = SettingsRepositoryImpl(apolloClient)

    @Provides
    fun categoryRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): CategoryRepository = CategoryRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun chapterRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): ChapterRepository = ChapterRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun downloadRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): DownloadRepository = DownloadRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun extensionRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): ExtensionRepository = ExtensionRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun globalRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): GlobalRepository = GlobalRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun libraryRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): LibraryRepository = LibraryRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun mangaRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): MangaRepository = MangaRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun sourceRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): SourceRepository = SourceRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun updatesRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): UpdatesRepository = UpdatesRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun backupRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): BackupRepository = BackupRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())
}
