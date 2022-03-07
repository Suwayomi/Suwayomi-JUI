/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.uicore.vm

import android.content.Context
import android.widget.Toast
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import me.tatarka.inject.annotations.Inject

actual class ContextWrapper @Inject constructor(val context: Context) {
    actual fun toPlatformString(stringResource: StringResource): String {
        return stringResource.desc().toString(context)
    }

    actual fun toPlatformString(stringResource: StringResource, vararg args: Any): String {
        return stringResource.format(*args).toString(context)
    }

    actual fun toast(string: String, length: Length) {
        Toast.makeText(
            context,
            string,
            when (length) {
                Length.SHORT -> Toast.LENGTH_SHORT
                Length.LONG -> Toast.LENGTH_LONG
            }
        ).show()
    }
}
