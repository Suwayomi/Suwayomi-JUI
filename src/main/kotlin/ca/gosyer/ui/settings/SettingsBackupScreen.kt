/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.min
import ca.gosyer.data.server.interactions.BackupInteractionHandler
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import ca.gosyer.util.system.CKLogger
import ca.gosyer.util.system.filePicker
import ca.gosyer.util.system.fileSaver
import com.github.zsoltk.compose.router.BackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class SettingsBackupViewModel @Inject constructor(
    private val backupHandler: BackupInteractionHandler
) : ViewModel() {
    private val _restoring = MutableStateFlow(false)
    val restoring = _restoring.asStateFlow()
    private val _restoreError = MutableStateFlow(false)
    val restoreError = _restoreError.asStateFlow()

    private val _creating = MutableStateFlow(false)
    val creating = _creating.asStateFlow()
    private val _creatingError = MutableStateFlow(false)
    val creatingError = _creatingError.asStateFlow()

    fun restoreFile(file: File?) {
        scope.launch {
            if (file == null || !file.exists()) {
                info { "Invalid file ${file?.absolutePath}" }
            } else {
                _restoreError.value = false
                _restoring.value = true
                try {
                    backupHandler.importBackupFile(file)
                } catch (e: Exception) {
                    info(e) { "Error importing backup" }
                    _restoreError.value = true
                } finally {
                    _restoring.value = false
                }
            }
        }
    }

    fun createFile(file: File?) {
        scope.launch {
            if (file == null) {
                info { "Invalid file ${file?.absolutePath}" }
            } else {
                if (file.exists()) file.delete()
                _creatingError.value = false
                _creating.value = true
                try {
                    val backup = backupHandler.exportBackupFile()
                } catch (e: Exception) {
                    info(e) { "Error exporting backup" }
                    _creatingError.value = true
                } finally {
                    _creating.value = false
                }
            }
        }
    }

    private companion object : CKLogger({})
}

@Composable
fun SettingsBackupScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsBackupViewModel>()
    val restoring by vm.restoring.collectAsState()
    val restoreError by vm.restoreError.collectAsState()
    val creating by vm.creating.collectAsState()
    val creatingError by vm.creatingError.collectAsState()
    Column {
        Toolbar("Backup Settings", navController, true)
        LazyColumn {
            item {
                PreferenceFile(
                    "Restore Backup",
                    "Restore a backup into Tachidesk",
                    restoring,
                    restoreError
                ) {
                    filePicker("json") {
                        vm.restoreFile(it.selectedFile)
                    }
                }
                PreferenceFile(
                    "Create Backup",
                    "Create a backup from Tachidesk",
                    creating,
                    creatingError
                ) {
                    fileSaver("test.json", "json") {
                        vm.createFile(it.selectedFile)
                    }
                }
            }
        }
    }
}

@Composable
fun PreferenceFile(title: String, subtitle: String, working: Boolean, error: Boolean, onClick: () -> Unit) {
    PreferenceRow(
        title = title,
        onClick = onClick,
        enabled = !working,
        subtitle = subtitle
    ) {
        BoxWithConstraints {
            val size = remember(maxHeight, maxWidth) {
                min(maxHeight, maxWidth) / 2
            }
            val modifier = Modifier.align(Alignment.Center)
                .size(size)
            if (working) {
                CircularProgressIndicator(
                    modifier
                )
            } else if (error) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = modifier,
                    tint = Color.Red
                )
            }
        }
    }
}
