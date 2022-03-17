/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.core.prefs

import android.content.Context
import com.russhwolf.settings.AndroidSettings
import me.tatarka.inject.annotations.Inject

actual class PreferenceStoreFactory @Inject constructor(private val context: Context) {
    actual fun create(vararg names: String): PreferenceStore {
        return AndroidPreferenceStore(
            AndroidSettings(
                context.getSharedPreferences(
                    names.joinToString(separator = "_"),
                    Context.MODE_PRIVATE
                )
            )
        )
    }
}
