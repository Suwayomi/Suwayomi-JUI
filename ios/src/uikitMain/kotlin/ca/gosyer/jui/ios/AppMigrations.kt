/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ios

import ca.gosyer.jui.domain.migration.service.MigrationPreferences
import ca.gosyer.jui.ios.build.BuildKonfig
import ca.gosyer.jui.uicore.vm.ContextWrapper
import me.tatarka.inject.annotations.Inject

@Inject
class AppMigrations(
    private val migrationPreferences: MigrationPreferences,
    private val contextWrapper: ContextWrapper,
) {
    fun runMigrations(): Boolean {
        val oldVersion = migrationPreferences.appVersion().get()
        if (oldVersion < BuildKonfig.MIGRATION_CODE) {
            migrationPreferences.appVersion().set(BuildKonfig.MIGRATION_CODE)

            // Fresh install
            if (oldVersion == 0) {
                return false
            }
            return true
        }
        return false
    }
}
