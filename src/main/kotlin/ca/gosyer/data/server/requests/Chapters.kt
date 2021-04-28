/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.requests

@Get
fun getMangaChaptersQuery(mangaId: Long) =
    "/api/v1/manga/$mangaId/chapters"

@Get
fun getChapterQuery(mangaId: Long, chapterIndex: Int) =
    "/api/v1/manga/$mangaId/chapter/$chapterIndex"

@Get
fun getPageQuery(mangaId: Long, chapterIndex: Int, index: Int) =
    "/api/v1/manga/$mangaId/chapter/$chapterIndex/page/$index"
