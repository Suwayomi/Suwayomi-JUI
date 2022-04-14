/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.requests

@Get
fun sourceListQuery() =
    "api/v1/source/list"

@Get
fun sourceInfoQuery(sourceId: Long) =
    "api/v1/source/$sourceId"

@Get
fun sourcePopularQuery(sourceId: Long, pageNum: Int) =
    "api/v1/source/$sourceId/popular/$pageNum"

@Get
fun sourceLatestQuery(sourceId: Long, pageNum: Int) =
    "api/v1/source/$sourceId/latest/$pageNum"

@Get
fun globalSearchQuery() =
    "api/v1/source/all/search"

@Get
fun sourceSearchQuery(sourceId: Long) =
    "api/v1/source/$sourceId/search"

@Get
fun getFilterListQuery(sourceId: Long) =
    "api/v1/source/$sourceId/filters"

@Post
fun setFilterRequest(sourceId: Long) =
    "api/v1/source/$sourceId/filters"

@Get
fun getSourceSettingsQuery(sourceId: Long) =
    "api/v1/source/$sourceId/preferences"

@Post
fun updateSourceSettingQuery(sourceId: Long) =
    "api/v1/source/$sourceId/preferences"
