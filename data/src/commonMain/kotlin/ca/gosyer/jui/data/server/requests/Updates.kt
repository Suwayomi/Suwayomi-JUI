/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.requests

@Get
fun recentUpdatesQuery(pageNum: Int) =
    "api/v1/update/recentChapters/$pageNum"

@Post
fun fetchUpdatesRequest() =
    "api/v1/update/fetch"

@Get
fun updatesSummaryQuery() =
    "api/v1/update/summary"

@WS
fun updatesQuery() =
    "api/v1/update"
