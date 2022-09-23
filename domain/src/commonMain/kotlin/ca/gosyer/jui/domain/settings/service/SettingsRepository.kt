/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.settings.service

import ca.gosyer.jui.domain.settings.model.About
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    @GET("api/v1/settings/about")
    fun aboutServer(): Flow<About>

    @POST("api/v1/settings/check-update")
    fun checkUpdate(): Flow<HttpResponse>
}
