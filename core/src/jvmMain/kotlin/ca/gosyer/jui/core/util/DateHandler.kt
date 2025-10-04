package ca.gosyer.jui.core.util

import androidx.compose.ui.text.intl.Locale
import ca.gosyer.jui.core.lang.toPlatform
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import me.tatarka.inject.annotations.Inject
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
            "" -> DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(Locale.current.toPlatform())
                .withZone(ZoneId.systemDefault())

            else -> DateTimeFormatter.ofPattern(format)
                .withZone(ZoneId.systemDefault())
        }.let { formatter ->
            {
                formatter.format(it.toJavaInstant())
            }
        }

    actual val dateTimeFormat: (Instant) -> String by lazy {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.current.toPlatform())
            .withZone(ZoneId.systemDefault())
            .let { formatter ->
                {
                    formatter.format(it.toJavaInstant())
                }
            }
    }
}
