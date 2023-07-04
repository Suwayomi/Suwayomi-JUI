/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.base

import androidx.compose.ui.text.intl.Locale
import ca.gosyer.jui.core.lang.toPlatform
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle

actual class DateHandler
    @Inject
    constructor() {
        actual val formatOptions by lazy {
            listOf(
                "",
                "MM/dd/yy",
                "dd/MM/yy",
                "yyyy-MM-dd",
            )
        }

        actual fun getDateFormat(format: String): (Instant) -> String =
            when (format) {
                "" -> NSDateFormatter()
                    .apply {
                        setDateStyle(NSDateFormatterShortStyle)
                        setTimeStyle(NSDateFormatterNoStyle)
                        setLocale(Locale.current.toPlatform())
                    }
                else -> NSDateFormatter()
                    .apply {
                        setDateFormat(format)
                    }
            }.let { formatter ->
                {
                    formatter.stringFromDate(it.toNSDate())
                }
            }

        actual val dateTimeFormat: (Instant) -> String by lazy {
            NSDateFormatter()
                .apply {
                    setDateStyle(NSDateFormatterShortStyle)
                    setTimeStyle(NSDateFormatterShortStyle)
                    setLocale(Locale.current.toPlatform())
                }
                .let { formatter ->
                    {
                        formatter.stringFromDate(it.toNSDate())
                    }
                }
        }
    }
