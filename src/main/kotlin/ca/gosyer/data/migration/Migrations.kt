/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.migration

import ca.gosyer.build.BuildConfig
import ca.gosyer.data.reader.ReaderPreferences
import javax.inject.Inject

class Migrations @Inject constructor(
    private val migrationPreferences: MigrationPreferences,
    private val readerPreferences: ReaderPreferences
) {

    fun runMigrations() {
        val code = migrationPreferences.version().get()
        if (code <= 0) {
            readerPreferences.modes().get().forEach {
                readerPreferences.getMode(it).direction().delete()
            }
            migrationPreferences.version().set(BuildConfig.MIGRATION_CODE)
        }
    }
}
