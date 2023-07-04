/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.prefs

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import me.tatarka.inject.annotations.Inject

actual class PreferenceStoreFactory
    @Inject
    constructor(private val context: Context) {
        actual fun create(vararg names: String): PreferenceStore {
            return StandardPreferenceStore(
                SharedPreferencesSettings(
                    context.getSharedPreferences(
                        names.joinToString(separator = "_"),
                        Context.MODE_PRIVATE,
                    ),
                ),
            )
        }
    }
