/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android

import ca.gosyer.jui.android.data.update.UpdateCheckWorker
import ca.gosyer.jui.data.migration.MigrationPreferences
import ca.gosyer.jui.uicore.vm.ContextWrapper
import me.tatarka.inject.annotations.Inject

class AppMigrations @Inject constructor(
    private val migrationPreferences: MigrationPreferences,
    private val contextWrapper: ContextWrapper
) {

    fun runMigrations(): Boolean {
        val oldVersion = migrationPreferences.appVersion().get()
        if (oldVersion < BuildConfig.VERSION_CODE) {
            migrationPreferences.appVersion().set(BuildConfig.VERSION_CODE)

            UpdateCheckWorker.setupTask(contextWrapper)

            // Fresh install
            if (oldVersion == 0) {
                return false
            }
            return true
        }
        return false
    }
}
