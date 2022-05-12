/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.lang

import androidx.compose.ui.text.intl.Locale
import java.util.Locale as PlatformLocale

fun Locale.toPlatform(): PlatformLocale = PlatformLocale.forLanguageTag(toLanguageTag())

actual fun Locale.getDisplayLanguage(displayLocale: Locale): String = toPlatform()
    .getDisplayLanguage(displayLocale.toPlatform())

actual fun Locale.getDisplayName(displayLocale: Locale): String = toPlatform()
    .getDisplayName(displayLocale.toPlatform())

actual val Locale.displayName: String get() = toPlatform().displayName
