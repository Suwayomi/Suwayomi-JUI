/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.lang

import androidx.compose.ui.text.intl.Locale
import ca.gosyer.jui.core.lang.toPlatform
import java.text.Collator as JvmCollator

actual fun Collator(locale: Locale): Collator {
    return Collator(JvmCollator.getInstance(locale.toPlatform()))
}

actual class Collator(private val jvmCollator: JvmCollator) {
    init {
        jvmCollator.strength = JvmCollator.PRIMARY
    }
    actual fun compare(source: String, target: String): Int {
        return jvmCollator.compare(source, target)
    }
}
