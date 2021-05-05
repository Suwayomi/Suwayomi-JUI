/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import ca.gosyer.util.system.filePicker
import com.github.zsoltk.compose.router.BackStack
import mu.KotlinLogging
import java.io.File
import javax.inject.Inject

class SettingsBackupViewModel @Inject constructor() : ViewModel() {
    private val logger = KotlinLogging.logger {}

    fun setFile(file: File?) {
        if (file == null || !file.exists()) {
            logger.info { "Invalid file ${file?.absolutePath}" }
        } else {
            logger.info { file.absolutePath }
        }
    }
}

@Composable
fun SettingsBackupScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsBackupViewModel>()
    Column {
        Toolbar("Backup Settings", navController, true)
        LazyColumn {
            item {
                PreferenceRow(
                    "Restore Backup",
                    onClick = {
                        filePicker {
                            vm.setFile(it.selectedFile)
                        }
                    }
                )
            }
        }
    }
}
