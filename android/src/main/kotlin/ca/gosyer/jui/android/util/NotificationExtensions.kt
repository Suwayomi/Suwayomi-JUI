/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android.util

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat

// This file is mostly from Tachiyomi

/**
 * Helper method to build a notification channel group.
 *
 * @param channelId the channel id.
 * @param block the function that will execute inside the builder.
 * @return a notification channel group to be displayed or updated.
 */
fun buildNotificationChannelGroup(
    channelId: String,
    block: (NotificationChannelGroupCompat.Builder.() -> Unit)
): NotificationChannelGroupCompat {
    val builder = NotificationChannelGroupCompat.Builder(channelId)
    builder.block()
    return builder.build()
}

/**
 * Helper method to build a notification channel.
 *
 * @param channelId the channel id.
 * @param channelImportance the channel importance.
 * @param block the function that will execute inside the builder.
 * @return a notification channel to be displayed or updated.
 */
fun buildNotificationChannel(
    channelId: String,
    channelImportance: Int,
    block: (NotificationChannelCompat.Builder.() -> Unit)
): NotificationChannelCompat {
    val builder = NotificationChannelCompat.Builder(channelId, channelImportance)
    builder.block()
    return builder.build()
}
