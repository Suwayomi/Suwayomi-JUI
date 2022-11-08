/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android.data.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import ca.gosyer.jui.android.util.buildNotificationChannel
import ca.gosyer.jui.android.util.buildNotificationChannelGroup
import ca.gosyer.jui.i18n.MR
import dev.icerock.moko.resources.desc.desc

object Notifications {

    /**
     * Notification channel and ids used by the downloader.
     */
    private const val GROUP_DOWNLOADER = "group_downloader"
    const val CHANNEL_DOWNLOADER_RUNNING = "downloader_channel"
    const val ID_DOWNLOADER_RUNNING = -101
    const val CHANNEL_DOWNLOADER_DOWNLOADING = "downloader_channel_downloading"
    const val ID_DOWNLOADER_DOWNLOADING = -102

    /**
     * Notification channel and ids used by the library updates.
     */
    private const val GROUP_LIBRARY = "group_library"
    const val CHANNEL_LIBRARY_UPDATES = "library_updates_channel"
    const val ID_LIBRARY_UPDATES = -301
    const val CHANNEL_LIBRARY_PROGRESS = "library_progress_channel"
    const val ID_LIBRARY_PROGRESS = -302

    /**
     * Notification channel and ids used for app updates.
     */
    private const val GROUP_APK_UPDATES = "group_apk_updates"
    const val CHANNEL_APP_UPDATE = "app_apk_update_channel"
    const val ID_UPDATES_TO_APP = -201
    const val CHANNEL_EXTENSIONS_UPDATE = "ext_apk_update_channel"
    const val ID_UPDATES_TO_EXTS = -202

    fun createChannels(context: Context) {
        val notificationService = NotificationManagerCompat.from(context)

        notificationService.createNotificationChannelGroupsCompat(
            listOf(
                buildNotificationChannelGroup(GROUP_DOWNLOADER) {
                    setName(MR.strings.group_downloader.desc().toString(context))
                },
                buildNotificationChannelGroup(GROUP_LIBRARY) {
                    setName(MR.strings.group_library.desc().toString(context))
                },
                buildNotificationChannelGroup(GROUP_APK_UPDATES) {
                    setName(MR.strings.group_updates.desc().toString(context))
                }
            )
        )

        notificationService.createNotificationChannelsCompat(
            listOf(
                buildNotificationChannel(
                    CHANNEL_DOWNLOADER_RUNNING,
                    NotificationManagerCompat.IMPORTANCE_LOW
                ) {
                    setName(MR.strings.channel_active.desc().toString(context))
                    setGroup(GROUP_DOWNLOADER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_DOWNLOADER_DOWNLOADING,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT
                ) {
                    setName(MR.strings.channel_progress.desc().toString(context))
                    setGroup(GROUP_DOWNLOADER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_LIBRARY_UPDATES,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT
                ) {
                    setName(MR.strings.channel_active.desc().toString(context))
                    setGroup(GROUP_LIBRARY)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_LIBRARY_PROGRESS,
                    NotificationManagerCompat.IMPORTANCE_LOW
                ) {
                    setName(MR.strings.channel_progress.desc().toString(context))
                    setGroup(GROUP_LIBRARY)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_APP_UPDATE,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT
                ) {
                    setGroup(GROUP_APK_UPDATES)
                    setName(MR.strings.group_updates_channel_app.desc().toString(context))
                },
                buildNotificationChannel(
                    CHANNEL_EXTENSIONS_UPDATE,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT
                ) {
                    setGroup(GROUP_APK_UPDATES)
                    setName(MR.strings.group_updates_channel_ext.desc().toString(context))
                }
            )
        )
    }
}
