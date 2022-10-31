/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import ca.gosyer.jui.core.io.Serializable as JvmSerializable

@Serializable
@Stable
data class Source(
    val id: Long,
    val name: String,
    val lang: String,
    val iconUrl: String,
    val supportsLatest: Boolean,
    val isConfigurable: Boolean,
    val isNsfw: Boolean,
    val displayName: String
) : JvmSerializable {
    val displayLang: String
        get() = if (id == LOCAL_SOURCE_ID) {
            "other"
        } else {
            lang
        }

    companion object {
        const val LOCAL_SOURCE_LANG = "localsourcelang"
        const val LOCAL_SOURCE_ID = 0L
    }
}
