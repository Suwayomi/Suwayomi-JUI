/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.vm

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import me.tatarka.inject.annotations.Inject

actual class ContextWrapper @Inject constructor(context: Context) : ContextWrapper(context) {
    actual fun toPlatformString(stringResource: StringResource): String {
        return stringResource.desc().toString(this)
    }

    actual fun toPlatformString(stringResource: StringResource, vararg args: Any): String {
        return stringResource.format(*args).toString(this)
    }

    actual fun toast(string: String, length: Length) {
        Toast.makeText(
            this,
            string,
            when (length) {
                Length.SHORT -> Toast.LENGTH_SHORT
                Length.LONG -> Toast.LENGTH_LONG
            }
        ).show()
    }
}
