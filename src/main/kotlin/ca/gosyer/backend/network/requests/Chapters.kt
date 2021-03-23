/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.network.requests

@Get
fun getMangaChaptersQuery(mangaId: Long) =
    "/api/v1/manga/$mangaId/chapters"

@Get
fun getChapterQuery(mangaId: Long, chapterId: Long) =
    "/api/v1/manga/$mangaId/chapter/$chapterId"

@Get
fun getPageQuery(mangaId: Long, chapterId: Long, index: Int) =
    "/api/v1/manga/$mangaId/chapter/$chapterId/page/$index"