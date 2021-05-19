/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Backup(
    val categories: List<List<@Contextual Any?>> = emptyList(),
    val mangas: List<Manga>,
    val version: Int = 1
) {
    @Serializable
    data class Manga(
        val manga: List<@Contextual Any?>,
        val chapters: List<Chapter> = emptyList(),
        val categories: List<String> = emptyList(),
        val history: List<List<@Contextual Any?>> = emptyList(),
        val track: List<Track> = emptyList()
    )

    @Serializable
    data class Chapter(
        @SerialName("u")
        val url: String,
        @SerialName("r")
        val read: Int = 0,
        @SerialName("b")
        val bookmarked: Int = 0,
        @SerialName("l")
        val lastRead: Int = 0
    )

    @Serializable
    data class Track(
        @SerialName("l")
        val lastRead: Int,
        @SerialName("ml")
        val libraryId: Int,
        @SerialName("r")
        val mediaId: Int,
        @SerialName("s")
        val syncId: Int,
        @SerialName("t")
        val title: String,
        @SerialName("u")
        val url: String
    )
}
