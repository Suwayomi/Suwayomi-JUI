/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.updates.service

import ca.gosyer.jui.domain.updates.model.Updates
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface UpdatesRepositoryOld {
    @GET("api/v1/update/recentChapters/{pageNum}/")
    fun getRecentUpdates(
        @Path("pageNum") pageNum: Int,
    ): Flow<Updates>

    @POST("api/v1/update/fetch/")
    fun updateLibrary(): Flow<HttpResponse>

    @POST("api/v1/update/fetch/")
    @FormUrlEncoded
    fun updateCategory(
        @Field("category") categoryId: Long,
    ): Flow<HttpResponse>
}
