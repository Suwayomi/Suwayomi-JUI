/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android.data.update

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ca.gosyer.jui.android.AppComponent
import ca.gosyer.jui.android.data.notification.Notifications
import ca.gosyer.jui.android.util.notificationBuilder
import ca.gosyer.jui.android.util.notificationManager
import ca.gosyer.jui.domain.updates.interactor.UpdateChecker.Update
import ca.gosyer.jui.i18n.MR
import dev.icerock.moko.resources.desc.desc
import java.util.concurrent.TimeUnit

class UpdateCheckWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val update = AppComponent.getInstance(context.applicationContext)
                .let {
                    if (it.updatePreferences.enabled().get()) {
                        it.updateChecker.await(false)
                    } else {
                        null
                    }
                }

            if (update is Update.UpdateFound) {
                context.notificationBuilder(Notifications.CHANNEL_APP_UPDATE) {
                    setContentTitle(MR.strings.new_update_title.desc().toString(context))
                    setContentText(update.release.version)
                    setSmallIcon(android.R.drawable.stat_sys_download_done)
                }.let {
                    context.notificationManager.notify(Notifications.ID_UPDATES_TO_APP, it.build())
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "UpdateChecker"

        fun setupTask(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                7,
                TimeUnit.DAYS,
                3,
                TimeUnit.HOURS,
            )
                .addTag(TAG)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        fun cancelTask(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
        }
    }
}
