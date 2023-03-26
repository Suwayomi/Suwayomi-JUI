/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about.licenses.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import ca.gosyer.jui.core.lang.withIOContext
import ca.gosyer.jui.i18n.MR
import com.mikepenz.aboutlibraries.Libs

@Composable
actual fun getLicenses(): Libs? {
    val libs by produceState<Libs?>(
        null,
    ) {
        withIOContext {
            val json = MR.files.aboutlibraries.readText()
            value = Libs.Builder().withJson(json).build()
        }
    }
    return libs
}
