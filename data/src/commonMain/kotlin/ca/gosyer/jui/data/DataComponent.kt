/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.core.lang.addSuffix
import ca.gosyer.jui.data.backup.BackupRepositoryImpl
import ca.gosyer.jui.data.chapter.ChapterRepositoryImpl
import ca.gosyer.jui.data.settings.SettingsRepositoryImpl
import ca.gosyer.jui.domain.backup.service.BackupRepository
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.settings.service.SettingsRepository
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.ws.GraphQLWsProtocol
import com.apollographql.ktor.ktorClient
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import me.tatarka.inject.annotations.Provides

interface DataComponent : SharedDataComponent {
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
    fun chapterRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): ChapterRepository = ChapterRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())

    @Provides
    fun backupRepository(
        apolloClient: ApolloClient,
        http: Http,
        serverPreferences: ServerPreferences,
    ): BackupRepository = BackupRepositoryImpl(apolloClient, http, serverPreferences.serverUrl().get())
}
