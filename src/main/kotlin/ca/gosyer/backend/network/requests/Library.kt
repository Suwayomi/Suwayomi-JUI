/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.network.requests

@Get
fun addMangaToLibraryQuery(mangaId: Long) =
    "/api/v1/manga/$mangaId/library"

@Delete
fun removeMangaFromLibraryRequest(mangaId: Long) =
    "/api/v1/manga/$mangaId/library"

@Get
fun getLibraryQuery() =
    "/api/v1/library/"