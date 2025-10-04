/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.library.service

import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun addMangaToLibrary(mangaId: Long): Flow<Unit>

    fun removeMangaFromLibrary(mangaId: Long): Flow<Unit>
}
