package ca.gosyer.jui.core.util

import kotlinx.datetime.Instant

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect class DateHandler {
    val formatOptions: List<String>

    fun getDateFormat(format: String): (Instant) -> String

    val dateTimeFormat: (Instant) -> String
}
