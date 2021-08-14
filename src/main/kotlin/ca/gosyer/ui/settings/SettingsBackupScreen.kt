/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.gosyer.data.server.interactions.BackupInteractionHandler
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.ui.base.WindowDialog
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import ca.gosyer.util.lang.throwIfCancellation
import ca.gosyer.util.system.CKLogger
import ca.gosyer.util.system.filePicker
import ca.gosyer.util.system.fileSaver
import com.github.zsoltk.compose.router.BackStack
import io.ktor.client.features.onDownload
import io.ktor.client.features.onUpload
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import javax.inject.Inject

class SettingsBackupViewModel @Inject constructor(
    private val backupHandler: BackupInteractionHandler,
    private val sourceHandler: SourceInteractionHandler
) : ViewModel() {
    private val _restoring = MutableStateFlow(false)
    val restoring = _restoring.asStateFlow()
    private val _restoringProgress = MutableStateFlow<Float?>(null)
    val restoringProgress = _restoringProgress.asStateFlow()
    private val _restoreStatus = MutableStateFlow<Status>(Status.Nothing)
    internal val restoreStatus = _restoreStatus.asStateFlow()
    private val _missingSourceFlow = MutableSharedFlow<Pair<File, List<String>>>()
    val missingSourceFlow = _missingSourceFlow.asSharedFlow()

    private val _creating = MutableStateFlow(false)
    val creating = _creating.asStateFlow()
    private val _creatingProgress = MutableStateFlow<Float?>(null)
    val creatingProgress = _creatingProgress.asStateFlow()
    private val _creatingStatus = MutableStateFlow<Status>(Status.Nothing)
    internal val creatingStatus = _creatingStatus.asStateFlow()

    fun restoreFile(file: File?) {
        scope.launch {
            if (file == null || !file.exists()) {
                info { "Invalid file ${file?.absolutePath}" }
                _restoreStatus.value = Status.Error
                _restoring.value = false
            } else {
                try {
                    val sources =
                        Json.decodeFromString<JsonObject>(file.readText())["extensions"]!!.jsonArray.associate {
                            val items = it.jsonPrimitive.content.split(":")
                            items[0].toLong() to items[1]
                        }
                    val installedSources = sourceHandler.getSourceList().associateBy { it.id }
                    val missingSources = sources.filter { installedSources[it.key] == null }
                    if (missingSources.isEmpty()) {
                        restoreBackup(file)
                    } else {
                        _missingSourceFlow.emit(file to missingSources.values.toList())
                    }
                } catch (e: Exception) {
                    info(e) { "Error importing backup" }
                    _restoreStatus.value = Status.Error
                    e.throwIfCancellation()
                }
            }
        }
    }

    fun restoreBackup(file: File) {
        scope.launch {
            _restoreStatus.value = Status.Nothing
            _restoringProgress.value = null
            _restoring.value = true
            try {
                backupHandler.importBackupFile(file) {
                    onUpload { bytesSentTotal, contentLength ->
                        _restoringProgress.value = (bytesSentTotal.toFloat() / contentLength).coerceAtMost(1.0F)
                    }
                }
                _restoreStatus.value = Status.Success
            } catch (e: Exception) {
                info(e) { "Error importing backup" }
                _restoreStatus.value = Status.Error
                e.throwIfCancellation()
            } finally {
                _restoring.value = false
            }
        }
    }

    fun stopRestore() {
        _restoreStatus.value = Status.Error
        _restoring.value = false
    }

    fun createFile(file: File?) {
        scope.launch {
            if (file == null) {
                info { "Invalid file ${file?.absolutePath}" }
            } else {
                if (file.exists()) file.delete()
                _creatingStatus.value = Status.Nothing
                _creatingProgress.value = null
                _creating.value = true
                try {
                    val backup = backupHandler.exportBackupFile {
                        onDownload { bytesSentTotal, contentLength ->
                            _creatingProgress.value = (bytesSentTotal.toFloat() / contentLength).coerceAtMost(0.99F)
                        }
                    }
                    file.outputStream().use {
                        backup.content.copyTo(it)
                    }
                    _creatingStatus.value = Status.Success
                } catch (e: Exception) {
                    info(e) { "Error exporting backup" }
                    _creatingStatus.value = Status.Error
                    e.throwIfCancellation()
                } finally {
                    _creatingProgress.value = 1.0F
                    _creating.value = false
                }
            }
        }
    }

    internal sealed class Status {
        object Nothing : Status()
        object Success : Status()
        object Error : Status()
    }

    private companion object : CKLogger({})
}

@Composable
fun SettingsBackupScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsBackupViewModel>()
    val restoring by vm.restoring.collectAsState()
    val restoringProgress by vm.restoringProgress.collectAsState()
    val restoreStatus by vm.restoreStatus.collectAsState()
    val creating by vm.creating.collectAsState()
    val creatingProgress by vm.creatingProgress.collectAsState()
    val creatingStatus by vm.creatingStatus.collectAsState()
    LaunchedEffect(Unit) {
        vm.missingSourceFlow.collect { (backup, sources) ->
            openMissingSourcesDialog(sources, { vm.restoreBackup(backup) }, vm::stopRestore)
        }
    }

    Column {
        Toolbar(stringResource("settings_backup_screen"), navController, true)
        LazyColumn {
            item {
                PreferenceFile(
                    stringResource("backup_restore"),
                    stringResource("backup_restore_sub"),
                    restoring,
                    restoringProgress,
                    restoreStatus
                ) {
                    filePicker("json") {
                        vm.restoreFile(it.selectedFile)
                    }
                }
                PreferenceFile(
                    stringResource("backup_create"),
                    stringResource("backup_create_sub"),
                    creating,
                    creatingProgress,
                    creatingStatus
                ) {
                    fileSaver("backup.json", "json") {
                        vm.createFile(it.selectedFile)
                    }
                }
            }
        }
    }
}

private fun openMissingSourcesDialog(missingSources: List<String>, onPositiveClick: () -> Unit, onNegativeClick: () -> Unit) {
    WindowDialog(
        "Missing Sources",
        onPositiveButton = onPositiveClick,
        onNegativeButton = onNegativeClick
    ) {
        LazyColumn {
            item {
                Text(stringResource("missing_sources"))
            }
            items(missingSources) {
                Text(it)
            }
        }
    }
}

@Composable
private fun PreferenceFile(title: String, subtitle: String, working: Boolean, progress: Float?, status: SettingsBackupViewModel.Status, onClick: () -> Unit) {
    PreferenceRow(
        title = title,
        onClick = onClick,
        enabled = !working,
        subtitle = subtitle
    ) {
        val modifier = Modifier.align(Alignment.Center)
            .size(24.dp)
        if (working) {
            if (progress != null) {
                CircularProgressIndicator(
                    progress,
                    modifier
                )
            } else {
                CircularProgressIndicator(
                    modifier
                )
            }
        } else if (status != SettingsBackupViewModel.Status.Nothing) {
            when (status) {
                SettingsBackupViewModel.Status.Error -> Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    modifier = modifier,
                    tint = Color.Red
                )
                SettingsBackupViewModel.Status.Success -> Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = modifier
                )
                else -> Unit
            }
        }
    }
}
