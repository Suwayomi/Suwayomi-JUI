/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.models

import ca.gosyer.data.server.interactions.MangaInteractionHandler
import ca.gosyer.i18n.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    val status: MangaStatus,
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
    ) = flow {
        if (readerMode != meta.juiReaderMode) {
            mangaHandler.updateMangaMeta(this@Manga, "juiReaderMode", readerMode)
                .collect()
        }
        emit(Unit)
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

@Serializable
enum class MangaStatus(@Transient val res: StringResource) {
    UNKNOWN(MR.strings.status_unknown),
    ONGOING(MR.strings.status_ongoing),
    COMPLETED(MR.strings.status_completed),
    LICENSED(MR.strings.status_Licensed);
}
