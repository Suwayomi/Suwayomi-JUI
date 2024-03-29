/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.library.model

import androidx.compose.runtime.Immutable
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.manga.model.Manga
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class UpdateStatus(
    val categoryStatusMap: Map<CategoryUpdateStatus, List<Category>> = emptyMap(),
    val mangaStatusMap: Map<JobStatus, List<Manga>> = emptyMap(),
    val running: Boolean,
)
