/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.core.di.AppScope
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
import ca.gosyer.jui.data.user.UserRepositoryImpl
import ca.gosyer.jui.data.util.ApolloAuthInterceptor
import ca.gosyer.jui.data.util.KtorWebSocketEngine
import ca.gosyer.jui.domain.backup.service.BackupRepository
import ca.gosyer.jui.domain.category.service.CategoryRepository
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.download.service.DownloadRepository
import ca.gosyer.jui.domain.extension.service.ExtensionRepository
import ca.gosyer.jui.domain.global.service.GlobalRepository
import ca.gosyer.jui.domain.library.service.LibraryRepository
import ca.gosyer.jui.domain.manga.service.MangaRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.HttpNoAuth
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.settings.service.SettingsRepository
import ca.gosyer.jui.domain.source.service.SourceRepository
import ca.gosyer.jui.domain.updates.service.UpdatesRepository
import ca.gosyer.jui.domain.user.interactor.UserRefreshUI
import ca.gosyer.jui.domain.user.service.UserRepository
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.network.websocket.GraphQLWsProtocol
import com.apollographql.apollo.network.websocket.WebSocketNetworkTransport
import com.apollographql.ktor.http.KtorHttpEngine
import io.ktor.client.HttpClient
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import korlibs.time.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Provides

typealias ApolloAppClient = StateFlow<ApolloClient>

typealias ApolloAppClientNoAuth = StateFlow<ApolloClient>

@OptIn(ApolloExperimental::class)
private fun getApolloClient(
    httpClient: HttpClient,
    serverUrl: Url,
    authInterceptor: ApolloAuthInterceptor?,
): ApolloClient {
    val url = URLBuilder(serverUrl)
        .appendPathSegments("api", "graphql")
        .buildString()
    return ApolloClient.Builder()
        .serverUrl(url)
        .httpEngine(KtorHttpEngine(httpClient))
        .apply {
            if (authInterceptor != null) {
                addInterceptor(authInterceptor)
            }
        }
        .subscriptionNetworkTransport(
            WebSocketNetworkTransport.Builder()
                .serverUrl(url)
                .pingInterval(30.seconds)
                .webSocketEngine(KtorWebSocketEngine(httpClient))
                .wsProtocol(GraphQLWsProtocol())
                .build()
        )
        .dispatcher(Dispatchers.IO)
        .build()
}

interface DataComponent : SharedDataComponent {

    @Provides
    @AppScope
    fun apolloAppClient(
        http: HttpNoAuth,
        serverPreferences: ServerPreferences,
        apolloAuthInterceptor: ApolloAuthInterceptor,
    ): ApolloAppClient =
        http
            .map { getApolloClient(it, serverPreferences.serverUrl().get(), apolloAuthInterceptor) }
            .stateIn(
                GlobalScope,
                SharingStarted.Eagerly,
                getApolloClient(http.value, serverPreferences.serverUrl().get(), apolloAuthInterceptor),
            )

    @Provides
    @AppScope
    fun apolloAppClientNoAuth(
        httpNoAuth: HttpNoAuth,
        serverPreferences: ServerPreferences,
        userRefreshUI: Lazy<UserRefreshUI>,
    ): ApolloAppClientNoAuth =
        httpNoAuth
            .map { getApolloClient(it, serverPreferences.serverUrl().get(), null) }
            .stateIn(
                GlobalScope,
                SharingStarted.Eagerly,
                getApolloClient(httpNoAuth.value, serverPreferences.serverUrl().get(), null),
            )

    @Provides
    fun settingsRepository(apolloAppClient: ApolloAppClient): SettingsRepository = SettingsRepositoryImpl(apolloAppClient)

    @Provides
    fun categoryRepository(
        apolloAppClient: ApolloAppClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): CategoryRepository = CategoryRepositoryImpl(apolloAppClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun chapterRepository(
        apolloAppClient: ApolloAppClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): ChapterRepository = ChapterRepositoryImpl(apolloAppClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun downloadRepository(
        apolloAppClient: ApolloAppClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): DownloadRepository = DownloadRepositoryImpl(apolloAppClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun extensionRepository(
        apolloAppClient: ApolloAppClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): ExtensionRepository = ExtensionRepositoryImpl(apolloAppClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun globalRepository(
        apolloAppClient: ApolloAppClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): GlobalRepository = GlobalRepositoryImpl(apolloAppClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun libraryRepository(
        apolloAppClient: ApolloAppClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): LibraryRepository = LibraryRepositoryImpl(apolloAppClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun mangaRepository(
        apolloAppClient: ApolloAppClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): MangaRepository = MangaRepositoryImpl(apolloAppClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun sourceRepository(
        apolloAppClient: ApolloAppClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): SourceRepository = SourceRepositoryImpl(apolloAppClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun updatesRepository(
        apolloAppClient: ApolloAppClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): UpdatesRepository = UpdatesRepositoryImpl(apolloAppClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun userRepository(
        apolloAppClient: ApolloAppClient,
        apolloAppClientNoAuth: ApolloAppClientNoAuth,
        http: Http,
        httpNoAuth: HttpNoAuth,
        serverPreferences: ServerPreferences,
    ): UserRepository = UserRepositoryImpl(apolloAppClient, apolloAppClientNoAuth, http, httpNoAuth,serverPreferences.serverUrl().get())

    @Provides
    fun backupRepository(
        apolloAppClient: ApolloAppClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): BackupRepository = BackupRepositoryImpl(apolloAppClient, http, serverPreferences.serverUrl().get())
}
