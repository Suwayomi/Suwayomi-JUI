/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about.licenses.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import ca.gosyer.jui.core.lang.withIOContext
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext

@Composable
actual fun getLicenses(): Libs? {
    val context = LocalContext.current
    val libs by produceState<Libs?>(
        null,
        context
    ) {
        withIOContext {
            value = Libs.Builder().withContext(context).build()
        }
    }
    return libs
}
