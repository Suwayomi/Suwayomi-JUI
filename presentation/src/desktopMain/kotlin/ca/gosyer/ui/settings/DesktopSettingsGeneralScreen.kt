/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import okio.Path.Companion.toPath
import okio.asResourceFileSystem
import java.util.Locale

actual fun Any.getResourceLanguages(): Map<String, String> = this::class.java.classLoader.asResourceFileSystem().list("/localization/".toPath())
    .asSequence()
    .drop(1)
    .map { it.name.substringBeforeLast('.') }
    .map { it.substringAfter("mokoBundle_") }
    .map(String::trim)
    .map { it.replace("-r", "-") }
    .filterNot(String::isBlank)
    .associateWith { Locale.forLanguageTag(it).getDisplayName(currentLocale) }
)