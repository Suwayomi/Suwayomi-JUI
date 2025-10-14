/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.manga.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.i18n.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Transient

@Immutable
data class Manga(
    val id: Long,
    val sourceId: Long,
    val url: String,
    val title: String,
    val thumbnailUrl: String?,
    val thumbnailUrlLastFetched: Long = 0,
    val initialized: Boolean,
    val artist: String?,
    val author: String?,
    val description: String?,
    val genre: List<String>,
    val status: MangaStatus,
    val inLibrary: Boolean,
    val source: Source?,
    val updateStrategy: UpdateStrategy,
    val freshData: Boolean,
    val meta: MangaMeta,
    val realUrl: String?,
    val lastFetchedAt: Long?,
    val chaptersLastFetchedAt: Long?,
    val inLibraryAt: Long,
    val unreadCount: Int?,
    val downloadCount: Int?,
    val chapterCount: Int?,
    val lastChapterReadTime: Long? = null,
    val age: Long?,
    val chaptersAge: Long?,
)

@Immutable
data class MangaMeta(
    val juiReaderMode: String = DEFAULT_READER_MODE,
) {
    companion object {
        const val DEFAULT_READER_MODE = "default"
    }
}

@Stable
enum class MangaStatus(
    @Transient val res: StringResource,
) {
    UNKNOWN(MR.strings.status_unknown),
    ONGOING(MR.strings.status_ongoing),
    COMPLETED(MR.strings.status_completed),
    LICENSED(MR.strings.status_licensed),
    PUBLISHING_FINISHED(MR.strings.status_publishing_finished),
    CANCELLED(MR.strings.status_cancelled),
    ON_HIATUS(MR.strings.status_on_hiatus),
}

@Stable
enum class UpdateStrategy {
    ALWAYS_UPDATE,
    ONLY_FETCH_ONCE,
}
