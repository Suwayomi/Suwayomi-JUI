/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.global.service

import ca.gosyer.jui.domain.global.model.GlobalMeta
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PATCH
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface GlobalRepository {
    @GET("api/v1/meta")
    fun getGlobalMeta(): Flow<GlobalMeta>

    @FormUrlEncoded
    @PATCH("api/v1/meta")
    fun updateGlobalMeta(
        @Field("key") key: String,
        @Field("value") value: String,
    ): Flow<HttpResponse>
}
