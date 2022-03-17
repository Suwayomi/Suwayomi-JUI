/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
@file:JvmName("JvmStringsKt")

package ca.gosyer.core.lang

import io.fluidsonic.locale.Locale
import io.fluidsonic.locale.toPlatform

fun String.capitalize(locale: Locale = Locale.getDefault()) =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }


actual fun String.uppercase(locale: Locale): String = uppercase(locale.toPlatform())

actual fun String.lowercase(locale: Locale): String = lowercase(locale.toPlatform())

actual fun Char.titlecase(locale: Locale): String = titlecase(locale.toPlatform())