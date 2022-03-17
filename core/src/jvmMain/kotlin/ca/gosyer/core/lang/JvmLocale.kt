/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.core.lang

import io.fluidsonic.locale.Locale
import io.fluidsonic.locale.toCommon
import io.fluidsonic.locale.toPlatform
import java.util.Locale as PlatformLocale

actual fun Locale.Companion.getDefault(): Locale = PlatformLocale.getDefault().toCommon()

actual fun Locale.getDisplayLanguage(displayLocale: Locale): String = toPlatform()
    .getDisplayLanguage(displayLocale.toPlatform())

actual fun Locale.getDisplayName(displayLocale: Locale): String = toPlatform()
    .getDisplayName(displayLocale.toPlatform())

actual val Locale.displayName get() = toPlatform().displayName