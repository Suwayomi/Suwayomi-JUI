/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.gosyer.core.io.copyTo
import ca.gosyer.core.lang.throwIfCancellation
import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.server.interactions.BackupInteractionHandler
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.components.VerticalScrollbar
import ca.gosyer.ui.base.components.rememberScrollbarAdapter
import ca.gosyer.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.util.system.filePicker
import ca.gosyer.ui.util.system.fileSaver
import ca.gosyer.uicore.resources.stringResource
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import io.ktor.client.features.onDownload
import io.ktor.client.features.onUpload
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.source

class SettingsBackupScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<SettingsBackupViewModel>()
        SettingsBackupScreenContent(
            restoring = vm.restoring.collectAsState().value,
            restoringProgress = vm.restoringProgress.collectAsState().value,
            restoreStatus = vm.restoreStatus.collectAsState().value,
            creating = vm.creating.collectAsState().value,
            creatingProgress = vm.creatingProgress.collectAsState().value,
            creatingStatus = vm.creatingStatus.collectAsState().value,
            missingSourceFlow = vm.missingSourceFlow,
            createFlow = vm.createFlow,
            restoreFile = vm::restoreFile,
            restoreBackup = vm::restoreBackup,
            stopRestore = vm::stopRestore,
            exportBackup = vm::exportBackup
        )
    }
}

class SettingsBackupViewModel @Inject constructor(
    private val backupHandler: BackupInteractionHandler,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
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
private fun SettingsBackupScreenContent(
    restoring: Boolean,
    restoringProgress: Float?,
    restoreStatus: SettingsBackupViewModel.Status,
    creating: Boolean,
    creatingProgress: Float?,
    creatingStatus: SettingsBackupViewModel.Status,
    missingSourceFlow: SharedFlow<Pair<Path, List<String>>>,
    createFlow: SharedFlow<Pair<String, (Path) -> Unit>>,
    restoreFile: (Path?) -> Unit,
    restoreBackup: (Path) -> Unit,
    stopRestore: () -> Unit,
    exportBackup: () -> Unit
) {
    var backupFile by remember { mutableStateOf<Path?>(null) }
    var missingSources by remember { mutableStateOf(emptyList<String>()) }
    val dialogState = rememberMaterialDialogState()
    LaunchedEffect(Unit) {
        launch {
            missingSourceFlow.collect { (backup, sources) ->
                backupFile = backup
                missingSources = sources
                dialogState.show()
            }
        }
        launch {
            createFlow.collect { (filename, function) ->
                fileSaver(filename, "proto.gz", onApprove = function)
            }
        }
    }

    Scaffold(
        topBar = {
            Toolbar(stringResource(MR.strings.settings_backup_screen))
        }
    ) {
        Box(Modifier.padding(it)) {
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
                        filePicker("gz", onApprove = restoreFile)
                    }
                    PreferenceFile(
                        stringResource(MR.strings.backup_create),
                        stringResource(MR.strings.backup_create_sub),
                        creating,
                        creatingProgress,
                        creatingStatus,
                        exportBackup
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
    MissingSourcesDialog(
        dialogState,
        missingSources,
        onPositiveClick = {
            restoreBackup(backupFile ?: return@MissingSourcesDialog)
        },
        onNegativeClick = stopRestore
    )
}

@Composable
private fun MissingSourcesDialog(
    state: MaterialDialogState,
    missingSources: List<String>,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {
    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_ok), onClick = onPositiveClick)
            negativeButton(stringResource(MR.strings.action_cancel), onClick = onNegativeClick)
        },
        properties = getMaterialDialogProperties(),
    ) {
        title("Missing Sources")
        Box {
            val listState = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state = listState) {
                item {
                    Text(stringResource(MR.strings.missing_sources), style = MaterialTheme.typography.subtitle2)
                }
                items(missingSources) {
                    Text(it)
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(listState),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
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
