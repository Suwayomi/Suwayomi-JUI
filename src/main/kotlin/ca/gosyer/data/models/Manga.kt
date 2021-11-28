/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.models

import ca.gosyer.data.server.interactions.MangaInteractionHandler
import kotlinx.serialization.Serializable

@Serializable
data class Manga(
    val id: Long,
    val sourceId: Long,
    val url: String,
    val title: String,
    val thumbnailUrl: String?,
    val initialized: Boolean,
    val artist: String?,
    val author: String?,
    val description: String?,
    val genre: List<String>,
    val status: String,
    val inLibrary: Boolean,
    val source: Source?,
    val freshData: Boolean,
    val meta: MangaMeta,
    val realUrl: String?,
    val inLibraryAt: Long,
    val unreadCount: Int?,
    val downloadCount: Int?
) {
    suspend fun updateRemote(
        mangaHandler: MangaInteractionHandler,
        readerMode: String = meta.juiReaderMode
    ) {
        if (readerMode != meta.juiReaderMode) {
            mangaHandler.updateMangaMeta(this, "juiReaderMode", readerMode)
        }
    }
}

@Serializable
data class MangaMeta(
    val juiReaderMode: String = DEFAULT_READER_MODE
) {
    companion object {
        const val DEFAULT_READER_MODE = "default"
    }
}
