package ca.gosyer.jui.core.util

import androidx.compose.ui.text.intl.Locale
import ca.gosyer.jui.core.lang.toPlatform
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

actual class DateHandler {
    @Inject
    constructor()

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
