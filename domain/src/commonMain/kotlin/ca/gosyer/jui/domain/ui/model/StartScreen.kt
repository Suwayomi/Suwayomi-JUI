/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.ui.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Stable
enum class StartScreen {
    Library,
    Updates,

    //  History,
    Sources,
    Extensions,
}
