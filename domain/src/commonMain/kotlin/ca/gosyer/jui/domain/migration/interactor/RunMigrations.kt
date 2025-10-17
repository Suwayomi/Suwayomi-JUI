/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.migration.interactor

import ca.gosyer.jui.domain.build.BuildKonfig
import ca.gosyer.jui.domain.migration.service.MigrationPreferences
import ca.gosyer.jui.domain.reader.service.ReaderPreferences
import ca.gosyer.jui.domain.server.model.Auth
import ca.gosyer.jui.domain.server.service.ServerPreferences
import me.tatarka.inject.annotations.Inject

@Inject
class RunMigrations(
    private val migrationPreferences: MigrationPreferences,
    private val readerPreferences: ReaderPreferences,
    private val serverPreferences: ServerPreferences,
) {
    fun runMigrations() {
        val oldVersion = migrationPreferences.version().get()
        if (oldVersion < BuildKonfig.MIGRATION_CODE) {
            migrationPreferences.version().set(BuildKonfig.MIGRATION_CODE)

            // Fresh install
            if (oldVersion == 0) {
                readerPreferences.modes().get().forEach {
                    readerPreferences.getMode(it).direction().delete()
                }
                return
            }

            if (oldVersion < 6) {
                val authPreference = serverPreferences.auth()
                @Suppress("DEPRECATION")
                if (authPreference.get() == Auth.DIGEST) {
                    authPreference.set(Auth.NONE)
                }
            }
        }
    }
}
