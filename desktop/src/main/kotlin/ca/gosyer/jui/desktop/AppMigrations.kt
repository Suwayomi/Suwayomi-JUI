/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.desktop

import ca.gosyer.appdirs.AppDirs
import ca.gosyer.jui.desktop.build.BuildConfig
import ca.gosyer.jui.domain.migration.service.MigrationPreferences
import ca.gosyer.jui.uicore.vm.ContextWrapper
import com.diamondedge.logging.logging
import me.tatarka.inject.annotations.Inject
import okio.FileSystem
import okio.Path.Companion.toPath

@Inject
class AppMigrations(
    private val migrationPreferences: MigrationPreferences,
    private val contextWrapper: ContextWrapper,
) {
    @Suppress("KotlinConstantConditions")
    fun runMigrations(): Boolean {
        val oldVersion = migrationPreferences.appVersion().get()
        if (oldVersion < BuildConfig.MIGRATION_CODE) {
            migrationPreferences.appVersion().set(BuildConfig.MIGRATION_CODE)

            // Fresh install
            if (oldVersion == 0) {
                return false
            }

            if (oldVersion < 5) {
                val oldDir = AppDirs("Tachidesk-JUI").getUserDataDir().toPath()
                val newDir = AppDirs("Suwayomi-JUI").getUserDataDir().toPath()
                try {
                    FileSystem.SYSTEM.list(oldDir)
                        .filter { FileSystem.SYSTEM.metadata(it).isDirectory }
                        .forEach { path ->
                            runCatching {
                                FileSystem.SYSTEM.atomicMove(path, newDir / path.name)
                            }.onFailure {
                                log.e(it) { "Failed to move directory ${path.name}" }
                            }
                        }
                } catch (e: Exception) {
                    log.e(e) { "Failed to run directory migration" }
                }
            }

            return true
        }
        return false
    }

    companion object {
        private val log = logging()
    }
}
