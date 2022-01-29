/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
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
import ca.gosyer.core.io.copyTo
import ca.gosyer.core.lang.throwIfCancellation
import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.server.interactions.BackupInteractionHandler
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.WindowDialog
import ca.gosyer.ui.base.components.MenuController
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.util.system.filePicker
import ca.gosyer.util.system.fileSaver
import dev.icerock.moko.resources.compose.stringResource
import io.ktor.client.features.onDownload
import io.ktor.client.features.onUpload
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.buffer
import okio.source

class SettingsBackupViewModel @Inject constructor(
    private val backupHandler: BackupInteractionHandler
) : ViewModel() {
    private val _restoring = MutableStateFlow(false)
    val restoring = _restoring.asStateFlow()
    private val _restoringProgress = MutableStateFlow<Float?>(null)
    val restoringProgress = _restoringProgress.asStateFlow()
    private val _restoreStatus = MutableStateFlow<Status>(Status.Nothing)
    internal val restoreStatus = _restoreStatus.asStateFlow()
    private val _missingSourceFlow = MutableSharedFlow<Pair<Path, List<String>>>()
    val missingSourceFlow = _missingSourceFlow.asSharedFlow()

    private val _creating = MutableStateFlow(false)
    val creating = _creating.asStateFlow()
    private val _creatingProgress = MutableStateFlow<Float?>(null)
    val creatingProgress = _creatingProgress.asStateFlow()
    private val _creatingStatus = MutableStateFlow<Status>(Status.Nothing)
    internal val creatingStatus = _creatingStatus.asStateFlow()
    private val _createFlow = MutableSharedFlow<Pair<String, (Path) -> Unit>>()
    val createFlow = _createFlow.asSharedFlow()

    fun restoreFile(file: Path?) {
        scope.launch {
            if (file == null || !FileSystem.SYSTEM.exists(file)) {
                info { "Invalid file ${file?.toString()}" }
                _restoreStatus.value = Status.Error
                _restoring.value = false
            } else {
                try {
                    val (missingSources) = backupHandler.validateBackupFile(file)
                    if (missingSources.isEmpty()) {
                        restoreBackup(file)
                    } else {
                        _missingSourceFlow.emit(file to missingSources)
                    }
                } catch (e: Exception) {
                    info(e) { "Error importing backup" }
                    _restoreStatus.value = Status.Error
                    e.throwIfCancellation()
                }
            }
        }
    }

    fun restoreBackup(file: Path) {
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

    fun exportBackup() {
        scope.launch {
            _creatingStatus.value = Status.Nothing
            _creatingProgress.value = null
            _creating.value = true
            val backup = try {
                backupHandler.exportBackupFile {
                    onDownload { bytesSentTotal, contentLength ->
                        _creatingProgress.value = (bytesSentTotal.toFloat() / contentLength).coerceAtMost(0.99F)
                    }
                }
            } catch (e: Exception) {
                info(e) { "Error exporting backup" }
                _creatingStatus.value = Status.Error
                e.throwIfCancellation()
                null
            }
            _creatingProgress.value = 1.0F
            if (backup != null && backup.status.isSuccess()) {
                _createFlow.emit(
                    (backup.headers["content-disposition"]?.substringAfter("filename=")?.trim('"') ?: "backup") to {
                        scope.launch {
                            try {
                                backup.content.toInputStream()
                                    .source()
                                    .copyTo(
                                        FileSystem.SYSTEM.sink(it).buffer()
                                    )
                                _creatingStatus.value = Status.Success
                            } catch (e: Exception) {
                                e.throwIfCancellation()
                                error(e) { "Error creating backup" }
                                _creatingStatus.value = Status.Error
                            } finally {
                                _creating.value = false
                            }
                        }
                    }
                )
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
fun SettingsBackupScreen(menuController: MenuController) {
    val vm = viewModel<SettingsBackupViewModel>()
    val restoring by vm.restoring.collectAsState()
    val restoringProgress by vm.restoringProgress.collectAsState()
    val restoreStatus by vm.restoreStatus.collectAsState()
    val creating by vm.creating.collectAsState()
    val creatingProgress by vm.creatingProgress.collectAsState()
    val creatingStatus by vm.creatingStatus.collectAsState()
    LaunchedEffect(Unit) {
        launch {
            vm.missingSourceFlow.collect { (backup, sources) ->
                openMissingSourcesDialog(sources, { vm.restoreBackup(backup) }, vm::stopRestore)
            }
        }
        launch {
            vm.createFlow.collect { (filename, function) ->
                fileSaver(filename, "proto.gz") {
                    function(it.selectedFile.toOkioPath())
                }
            }
        }
    }

    Column {
        Toolbar(stringResource(MR.strings.settings_backup_screen), menuController, true)
        Box {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
                item {
                    PreferenceFile(
                        stringResource(MR.strings.backup_restore),
                        stringResource(MR.strings.backup_restore_sub),
                        restoring,
                        restoringProgress,
                        restoreStatus
                    ) {
                        filePicker("gz") {
                            vm.restoreFile(it.selectedFile.toOkioPath())
                        }
                    }
                    PreferenceFile(
                        stringResource(MR.strings.backup_create),
                        stringResource(MR.strings.backup_create_sub),
                        creating,
                        creatingProgress,
                        creatingStatus,
                        vm::exportBackup
                    )
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
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
                Text(stringResource(MR.strings.missing_sources), style = MaterialTheme.typography.subtitle2)
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
