/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.requests

@Get
fun sourceListQuery() =
    "/api/v1/source/list"

@Get
fun sourceInfoQuery(sourceId: Long) =
    "/api/v1/source/$sourceId"

@Get
fun sourcePopularQuery(sourceId: Long, pageNum: Int) =
    "/api/v1/source/$sourceId/popular/$pageNum"

@Get
fun sourceLatestQuery(sourceId: Long, pageNum: Int) =
    "/api/v1/source/$sourceId/latest/$pageNum"

@Get
fun globalSearchQuery(searchTerm: String) =
    "/api/v1/search/$searchTerm"

@Get
fun sourceSearchQuery(sourceId: Long, searchTerm: String, pageNum: Int) =
    "/api/v1/source/$sourceId/search/$searchTerm/$pageNum"

@Get
fun getFilterListQuery(sourceId: Long) =
    "/api/v1/source/$sourceId/filters/"