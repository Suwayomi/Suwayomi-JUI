/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.lang

import androidx.compose.ui.text.intl.Locale
import platform.Foundation.NSString
import platform.Foundation.localizedCaseInsensitiveCompare

actual class CollatorComparator : Comparator<String> {
    constructor()
    actual constructor(locale: Locale) : this()

    actual override fun compare(
        source: String,
        target: String,
    ): Int {
        @Suppress("CAST_NEVER_SUCCEEDS")
        return (source as NSString).localizedCaseInsensitiveCompare(target).toInt()
    }
}
