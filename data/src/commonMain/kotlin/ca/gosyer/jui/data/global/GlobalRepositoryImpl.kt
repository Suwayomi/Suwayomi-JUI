/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.global

import ca.gosyer.jui.data.graphql.GetGlobalMetaQuery
import ca.gosyer.jui.data.graphql.SetGlobalMetaMutation
import ca.gosyer.jui.domain.global.model.GlobalMeta
import ca.gosyer.jui.domain.global.service.GlobalRepository
import ca.gosyer.jui.domain.server.Http
import com.apollographql.apollo.ApolloClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GlobalRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val http: Http,
    private val serverUrl: Url,
) : GlobalRepository {
    override fun getGlobalMeta(): Flow<GlobalMeta> =
        apolloClient.query(
            GetGlobalMetaQuery(),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                GlobalMeta(data.metas.nodes.find { it.key == "example" }?.value?.toIntOrNull() ?: 0)
            }

    override fun updateGlobalMeta(
        key: String,
        value: String,
    ): Flow<Unit> =
        apolloClient.mutation(
            SetGlobalMetaMutation(key, value),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.setGlobalMeta!!.clientMutationId
            }
}
