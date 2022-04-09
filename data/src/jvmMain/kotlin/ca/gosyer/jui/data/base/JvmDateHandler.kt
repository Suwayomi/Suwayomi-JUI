/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.base

import ca.gosyer.jui.core.lang.getDefault
import io.fluidsonic.locale.Locale
import io.fluidsonic.locale.toPlatform
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import me.tatarka.inject.annotations.Inject
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

actual class DateHandler @Inject constructor() {
    actual val formatOptions by lazy {
        listOf(
            "",
            "MM/dd/yy",
            "dd/MM/yy",
            "yyyy-MM-dd"
        )
    }

    actual fun getDateFormat(format: String): (Instant) -> String = when (format) {
        "" -> DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(Locale.getDefault().toPlatform())
            .withZone(ZoneId.systemDefault())
        else -> DateTimeFormatter.ofPattern(format)
            .withZone(ZoneId.systemDefault())
    }.let { formatter ->
        {
            formatter.format(it.toJavaInstant())
        }
    }

    actual val dateTimeFormat: (Instant) -> String by lazy {
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault().toPlatform())
            .withZone(ZoneId.systemDefault())
            .let { formatter ->
                {
                    formatter.format(it.toJavaInstant())
                }
            }
    }
}
