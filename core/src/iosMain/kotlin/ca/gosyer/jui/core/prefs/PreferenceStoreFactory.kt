/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.prefs

import com.russhwolf.settings.NSUserDefaultsSettings
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSUserDefaults

actual class PreferenceStoreFactory {
    @Inject
    constructor()

    actual fun create(vararg names: String): PreferenceStore =
        StandardPreferenceStore(
            NSUserDefaultsSettings(
                NSUserDefaults.standardUserDefaults,
            ),
        )
}
