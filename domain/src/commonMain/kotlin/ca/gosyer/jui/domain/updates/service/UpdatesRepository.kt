/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.updates.service

import ca.gosyer.jui.domain.updates.model.Updates
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface UpdatesRepository {
    fun getRecentUpdates(pageNum: Int): Flow<Updates>
    fun updateLibrary(): Flow<HttpResponse>
    fun updateCategory(categoryId: Long): Flow<HttpResponse>
}
