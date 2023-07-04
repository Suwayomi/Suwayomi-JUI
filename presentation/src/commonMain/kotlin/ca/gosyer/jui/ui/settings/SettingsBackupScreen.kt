/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
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
import ca.gosyer.jui.core.io.SYSTEM
import ca.gosyer.jui.core.io.copyTo
import ca.gosyer.jui.core.io.saveTo
import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.core.lang.throwIfCancellation
import ca.gosyer.jui.domain.backup.interactor.ExportBackupFile
import ca.gosyer.jui.domain.backup.interactor.ImportBackupFile
import ca.gosyer.jui.domain.backup.interactor.ValidateBackupFile
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.jui.ui.base.file.rememberFileChooser
import ca.gosyer.jui.ui.base.file.rememberFileSaver
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.PreferenceRow
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.ui.util.lang.toSource
import ca.gosyer.jui.ui.viewModel
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.insets.navigationBars
import ca.gosyer.jui.uicore.insets.statusBars
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.listItems
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.onUpload
import io.ktor.client.statement.bodyAsChannel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.tatarka.inject.annotations.Inject
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source
import okio.buffer
import org.lighthousegames.logging.logging
import kotlin.random.Random

class SettingsBackupScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel { settingsBackupViewModel() }
        SettingsBackupScreenContent(
            restoreStatus = vm.restoreStatus.collectAsState().value,
            creatingStatus = vm.creatingStatus.collectAsState().value,
            missingSourceFlowHolder = vm.missingSourceFlowHolder,
            createFlowHolder = vm.createFlowHolder,
            restoreFile = vm::restoreFile,
            restoreBackup = vm::restoreBackup,
            stopRestore = vm::stopRestore,
            exportBackup = vm::exportBackup,
            exportBackupFileFound = vm::exportBackupFileFound,
        )
    }
}

class SettingsBackupViewModel
    @Inject
    constructor(
        private val validateBackupFile: ValidateBackupFile,
        private val importBackupFile: ImportBackupFile,
        private val exportBackupFile: ExportBackupFile,
        contextWrapper: ContextWrapper,
    ) : ViewModel(contextWrapper) {
        private val _restoreStatus = MutableStateFlow<Status>(Status.Nothing)
        val restoreStatus = _restoreStatus.asStateFlow()

        private val _missingSourceFlow = MutableSharedFlow<Pair<Path, ImmutableList<String>>>()
        val missingSourceFlowHolder = StableHolder(_missingSourceFlow.asSharedFlow())

        private val _creatingStatus = MutableStateFlow<Status>(Status.Nothing)
        val creatingStatus = _creatingStatus.asStateFlow()
        private val _createFlow = MutableSharedFlow<String>()
        val createFlowHolder = StableHolder(_createFlow.asSharedFlow())
        fun restoreFile(source: Source) {
            scope.launch {
                val file = try {
                    FileSystem.SYSTEM_TEMPORARY_DIRECTORY
                        .resolve("tachidesk.${Random.nextLong()}.proto.gz")
                        .also { file ->
                            source.saveTo(file)
                        }
                } catch (e: Exception) {
                    log.warn(e) { "Error creating backup file" }
                    _restoreStatus.value = Status.Error
                    e.throwIfCancellation()
                    return@launch
                }

                validateBackupFile.asFlow(file)
                    .onEach { (missingSources) ->
                        if (missingSources.isEmpty()) {
                            restoreBackup(file)
                        } else {
                            _missingSourceFlow.emit(file to missingSources.toImmutableList())
                        }
                    }
                    .catch {
                        toast(it.message.orEmpty())
                        log.warn(it) { "Error importing backup" }
                        _restoreStatus.value = Status.Error
                    }
                    .collect()
            }
        }

        fun restoreBackup(file: Path) {
            importBackupFile
                .asFlow(file) {
                    onUpload { bytesSentTotal, contentLength ->
                        _restoreStatus.value = Status.InProgress(
                            (bytesSentTotal.toFloat() / contentLength)
                                .coerceAtMost(1.0F),
                        )
                    }
                }
                .onStart {
                    _restoreStatus.value = Status.InProgress(null)
                }
                .onEach {
                    _restoreStatus.value = Status.Success
                }
                .catch {
                    toast(it.message.orEmpty())
                    log.warn(it) { "Error importing backup" }
                    _restoreStatus.value = Status.Error
                }
                .launchIn(scope)
        }

        fun stopRestore() {
            _restoreStatus.value = Status.Error
        }

        private val tempFile = MutableStateFlow<Path?>(null)
        private val mutex = Mutex()

        fun exportBackup() {
            exportBackupFile
                .asFlow {
                    onDownload { bytesSentTotal, contentLength ->
                        _creatingStatus.value = Status.InProgress(
                            (bytesSentTotal.toFloat() / contentLength)
                                .coerceAtMost(0.99F),
                        )
                    }
                }
                .onStart {
                    _creatingStatus.value = Status.InProgress(null)
                }
                .onEach { backup ->
                    val filename =
                        backup.headers["content-disposition"]?.substringAfter("filename=")
                            ?.trim('"') ?: "backup"
                    tempFile.value = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve(filename).also {
                        mutex.tryLock()
                        scope.launch {
                            try {
                                backup.bodyAsChannel().toSource().saveTo(it)
                            } catch (e: Exception) {
                                e.throwIfCancellation()
                                log.warn(e) { "Error creating backup" }
                                _creatingStatus.value = Status.Error
                            } finally {
                                mutex.unlock()
                            }
                        }
                    }
                    _createFlow.emit(filename)
                }
                .catch {
                    toast(it.message.orEmpty())
                    log.warn(it) { "Error exporting backup" }
                    _creatingStatus.value = Status.Error
                }
                .launchIn(scope)
        }

        fun exportBackupFileFound(backupSink: Sink) {
            scope.launch {
                mutex.withLock {
                    val tempFile = tempFile.value
                    if (creatingStatus.value is Status.InProgress && tempFile != null) {
                        try {
                            FileSystem.SYSTEM.source(tempFile).copyTo(backupSink.buffer())
                            _creatingStatus.value = Status.Success
                        } catch (e: Exception) {
                            e.throwIfCancellation()
                            log.error(e) { "Error moving created backup" }
                            _creatingStatus.value = Status.Error
                        }
                    } else {
                        _creatingStatus.value = Status.Error
                    }
                }
            }
        }

        private companion object {
            private val log = logging()
        }
    }

sealed class Status {
    object Nothing : Status()
    data class InProgress(val progress: Float?) : Status()
    object Success : Status()
    object Error : Status()
}

@Composable
private fun SettingsBackupScreenContent(
    restoreStatus: Status,
    creatingStatus: Status,
    missingSourceFlowHolder: StableHolder<SharedFlow<Pair<Path, ImmutableList<String>>>>,
    createFlowHolder: StableHolder<SharedFlow<String>>,
    restoreFile: (Source) -> Unit,
    restoreBackup: (Path) -> Unit,
    stopRestore: () -> Unit,
    exportBackup: () -> Unit,
    exportBackupFileFound: (Sink) -> Unit,
) {
    var backupFile by remember { mutableStateOf<Path?>(null) }
    var missingSources: ImmutableList<String> by remember { mutableStateOf(persistentListOf()) }
    val dialogState = rememberMaterialDialogState()
    val fileSaver = rememberFileSaver(exportBackupFileFound)
    val fileChooser = rememberFileChooser(restoreFile)
    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            missingSourceFlowHolder.item.collect { (backup, sources) ->
                backupFile = backup
                missingSources = sources
                dialogState.show()
            }
        }
        launch(Dispatchers.IO) {
            createFlowHolder.item.collect { filename ->
                fileSaver.save(filename)
            }
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal),
            ),
        ),
        topBar = {
            Toolbar(stringResource(MR.strings.settings_backup_screen))
        },
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                contentPadding = WindowInsets.bottomNav.add(
                    WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom,
                    ),
                ).asPaddingValues(),
            ) {
                item {
                    PreferenceFile(
                        stringResource(MR.strings.backup_restore),
                        stringResource(MR.strings.backup_restore_sub),
                        restoreStatus,
                    ) {
                        fileChooser.launch("gz")
                    }
                    PreferenceFile(
                        stringResource(MR.strings.backup_create),
                        stringResource(MR.strings.backup_create_sub),
                        creatingStatus,
                        exportBackup,
                    )
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .scrollbarPadding()
                    .windowInsetsPadding(
                        WindowInsets.bottomNav.add(
                            WindowInsets.navigationBars.only(
                                WindowInsetsSides.Bottom,
                            ),
                        ),
                    ),
            )
        }
    }
    MissingSourcesDialog(
        dialogState,
        missingSources,
        onPositiveClick = {
            restoreBackup(backupFile ?: return@MissingSourcesDialog)
        },
        onNegativeClick = stopRestore,
    )
}

@Composable
private fun MissingSourcesDialog(
    state: MaterialDialogState,
    missingSources: ImmutableList<String>,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit,
) {
    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_ok), onClick = onPositiveClick)
            negativeButton(stringResource(MR.strings.action_cancel), onClick = onNegativeClick)
        },
        properties = getMaterialDialogProperties(),
    ) {
        title(stringResource(MR.strings.missing_sources))
        Box {
            val listState = rememberLazyListState()
            listItems(missingSources, state = listState)
            VerticalScrollbar(
                rememberScrollbarAdapter(listState),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .scrollbarPadding(),
            )
        }
    }
}

@Composable
private fun PreferenceFile(
    title: String,
    subtitle: String,
    status: Status,
    onClick: () -> Unit,
) {
    PreferenceRow(
        title = title,
        onClick = onClick,
        enabled = status !is Status.InProgress,
        subtitle = subtitle,
    ) {
        val modifier = Modifier.align(Alignment.Center)
            .size(24.dp)
        if (status is Status.InProgress) {
            if (status.progress != null && !status.progress.isNaN()) {
                CircularProgressIndicator(
                    progress = status.progress,
                    modifier = modifier,
                )
            } else {
                CircularProgressIndicator(modifier)
            }
        } else if (status != Status.Nothing) {
            when (status) {
                Status.Error -> Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    modifier = modifier,
                    tint = Color.Red,
                )
                Status.Success -> Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = modifier,
                )
                else -> Unit
            }
        }
    }
}
