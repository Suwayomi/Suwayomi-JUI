/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.migration

import ca.gosyer.jui.data.migration.MigrationPreferences
import ca.gosyer.jui.data.reader.ReaderPreferences
import ca.gosyer.jui.domain.build.BuildKonfig
import me.tatarka.inject.annotations.Inject

class RunMigrations @Inject constructor(
    private val migrationPreferences: MigrationPreferences,
    private val readerPreferences: ReaderPreferences
) {

    fun runMigrations() {
        val code = migrationPreferences.version().get()
        if (code <= 0) {
            readerPreferences.modes().get().forEach {
                readerPreferences.getMode(it).direction().delete()
            }
            migrationPreferences.version().set(BuildKonfig.MIGRATION_CODE)
            return
        }
    }
}
