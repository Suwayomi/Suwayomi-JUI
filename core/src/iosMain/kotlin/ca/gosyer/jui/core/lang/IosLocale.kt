/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.lang

import androidx.compose.ui.text.intl.Locale
import platform.Foundation.localizedStringForLanguageCode
import platform.Foundation.localizedStringForLocaleIdentifier
import platform.Foundation.NSLocale as PlatformLocale

fun Locale.toPlatform(): PlatformLocale = PlatformLocale(toLanguageTag())

/**
 * First Locale: en_IN
 * Language: English
 */
actual fun Locale.getDisplayLanguage(displayLocale: Locale): String =
    toPlatform()
        .localizedStringForLanguageCode(displayLocale.toLanguageTag())!!

/**
 * First Locale: en_US
 * Language: English (United States)
 */
actual fun Locale.getDisplayName(displayLocale: Locale): String =
    toPlatform()
        .localizedStringForLocaleIdentifier(displayLocale.toLanguageTag())

actual val Locale.displayName: String get() = getDisplayLanguage(this)
