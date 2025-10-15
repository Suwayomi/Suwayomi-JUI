/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import ca.gosyer.jui.domain.server.model.Auth
import ca.gosyer.jui.domain.server.service.ServerHostPreferences
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.server.service.ServerService
import ca.gosyer.jui.domain.settings.model.AuthMode
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.prefs.ChoicePreference
import ca.gosyer.jui.ui.base.prefs.EditTextPreference
import ca.gosyer.jui.ui.base.prefs.PreferenceRow
import ca.gosyer.jui.ui.base.prefs.SwitchPreference
import ca.gosyer.jui.ui.util.system.folderPicker
import ca.gosyer.jui.uicore.prefs.asStateIn
import ca.gosyer.jui.uicore.prefs.asStringStateIn
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import me.tatarka.inject.annotations.Inject

@Composable
actual fun getServerHostItems(viewModel: @Composable () -> SettingsServerHostViewModel): LazyListScope.() -> Unit {
    val serverVm = viewModel()
    val hostValue by serverVm.host.collectAsState()
    val authMode by serverVm.hostAuthMode.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            serverVm.restartServer()
        }
    }

    return {
        ServerHostItems(
            hostValue = hostValue,
            authEnabledValue = authMode != AuthMode.NONE,
            serverSettingChanged = serverVm::serverSettingChanged,
            host = serverVm.host,
            ip = serverVm.ip,
            port = serverVm.port,
            rootPath = serverVm.rootPath,
            downloadPath = serverVm.downloadPath,
            backupPath = serverVm.backupPath,
            localSourcePath = serverVm.localSourcePath,
            authMode = serverVm.hostAuthMode,
            authUsername = serverVm.hostAuthUsername,
            authPassword = serverVm.hostAuthPassword,
        )
    }
}

@Inject
actual class SettingsServerHostViewModel(
    serverPreferences: ServerPreferences,
    serverHostPreferences: ServerHostPreferences,
    private val serverService: ServerService,
    contextWrapper: ContextWrapper,
) : ViewModel(contextWrapper) {
    val host = serverHostPreferences.host().asStateIn(scope)

    // IP
    val ip = serverHostPreferences.ip().asStateIn(scope)
    val port = serverHostPreferences.port().asStringStateIn(scope)

    // Root
    val rootPath = serverHostPreferences.rootPath().asStateIn(scope)

    // Downloader
    val downloadPath = serverHostPreferences.downloadPath().asStateIn(scope)

    // Backup
    val backupPath = serverHostPreferences.backupPath().asStateIn(scope)

    // LocalSource
    val localSourcePath = serverHostPreferences.localSourcePath().asStateIn(scope)

    // Authentication
    val hostAuthMode = serverHostPreferences.authMode().asStateIn(scope)
    val hostAuthUsername = serverHostPreferences.authUsername().asStateIn(scope)
    val hostAuthPassword = serverHostPreferences.authPassword().asStateIn(scope)

    private val _serverSettingChanged = MutableStateFlow(false)
    val serverSettingChanged = _serverSettingChanged.asStateFlow()

    fun serverSettingChanged() {
        _serverSettingChanged.value = true
    }

    fun restartServer() {
        if (serverSettingChanged.value) {
            serverService.startServer()
        }
    }

    // Handle password connection to hosted server
    val auth = serverPreferences.auth().asStateIn(scope)
    val authUsername = serverPreferences.authUsername().asStateIn(scope)
    val authPassword = serverPreferences.authPassword().asStateIn(scope)

    init {
        combine(host, hostAuthMode, hostAuthUsername, hostAuthPassword) { host, mode, username, password ->
            if (host) {
                when (mode) {
                    AuthMode.NONE -> auth.value = Auth.NONE
                    AuthMode.BASIC_AUTH -> {
                        auth.value = Auth.BASIC
                        authUsername.value = username
                        authPassword.value = password
                    }
                    AuthMode.SIMPLE_LOGIN -> auth.value = Auth.SIMPLE
                    AuthMode.UI_LOGIN -> auth.value = Auth.UI
                    AuthMode.UNKNOWN__ -> Unit
                }
            }
        }.launchIn(scope)
    }
}

fun LazyListScope.ServerHostItems(
    hostValue: Boolean,
    authEnabledValue: Boolean,
    serverSettingChanged: () -> Unit,
    host: MutableStateFlow<Boolean>,
    ip: MutableStateFlow<String>,
    port: MutableStateFlow<String>,
    rootPath: MutableStateFlow<String>,
    downloadPath: MutableStateFlow<String>,
    backupPath: MutableStateFlow<String>,
    localSourcePath: MutableStateFlow<String>,
    authMode: MutableStateFlow<AuthMode>,
    authUsername: MutableStateFlow<String>,
    authPassword: MutableStateFlow<String>,
) {
    item {
        SwitchPreference(preference = host, title = stringResource(MR.strings.host_server))
    }
    if (hostValue) {
        item {
            PreferenceRow(
                stringResource(MR.strings.host_settings),
                Icons.Rounded.Info,
                subtitle = stringResource(MR.strings.host_settings_sub),
            )
        }
        item {
            val ipValue by ip.collectAsState()
            EditTextPreference(
                preference = ip,
                title = stringResource(MR.strings.host_ip),
                subtitle = stringResource(MR.strings.host_ip_sub, ipValue),
                changeListener = serverSettingChanged,
            )
        }
        item {
            val portValue by port.collectAsState()
            EditTextPreference(
                preference = port,
                title = stringResource(MR.strings.host_port),
                subtitle = stringResource(MR.strings.host_port_sub, portValue),
                changeListener = serverSettingChanged,
            )
        }
        item {
            val rootPathValue by rootPath.collectAsState()
            PreferenceRow(
                title = stringResource(MR.strings.host_root_path),
                subtitle = if (rootPathValue.isEmpty()) {
                    stringResource(MR.strings.host_root_path_sub_empty)
                } else {
                    stringResource(MR.strings.host_root_path_sub, rootPathValue)
                },
                onClick = {
                    folderPicker {
                        rootPath.value = it.toString()
                        serverSettingChanged()
                    }
                },
                onLongClick = {
                    rootPath.value = ""
                    serverSettingChanged()
                },
            )
        }
        item {
            val downloadPathValue by downloadPath.collectAsState()
            PreferenceRow(
                title = stringResource(MR.strings.host_download_path),
                subtitle = if (downloadPathValue.isEmpty()) {
                    stringResource(MR.strings.host_download_path_sub_empty)
                } else {
                    stringResource(MR.strings.host_download_path_sub, downloadPathValue)
                },
                onClick = {
                    folderPicker {
                        downloadPath.value = it.toString()
                        serverSettingChanged()
                    }
                },
                onLongClick = {
                    downloadPath.value = ""
                    serverSettingChanged()
                },
            )
        }
        item {
            val backupPathValue by backupPath.collectAsState()
            PreferenceRow(
                title = stringResource(MR.strings.host_backup_path),
                subtitle = if (backupPathValue.isEmpty()) {
                    stringResource(MR.strings.host_backup_path_sub_empty)
                } else {
                    stringResource(MR.strings.host_backup_path_sub, backupPathValue)
                },
                onClick = {
                    folderPicker {
                        backupPath.value = it.toString()
                        serverSettingChanged()
                    }
                },
                onLongClick = {
                    backupPath.value = ""
                    serverSettingChanged()
                },
            )
        }
        item {
            val localSourcePathValue by localSourcePath.collectAsState()
            PreferenceRow(
                title = stringResource(MR.strings.host_local_source_path),
                subtitle = if (localSourcePathValue.isEmpty()) {
                    stringResource(MR.strings.host_local_source_path_sub_empty)
                } else {
                    stringResource(MR.strings.host_local_source_path_sub, localSourcePathValue)
                },
                onClick = {
                    folderPicker {
                        localSourcePath.value = it.toString()
                        serverSettingChanged()
                    }
                },
                onLongClick = {
                    localSourcePath.value = ""
                    serverSettingChanged()
                },
            )
        }
        item {
            ChoicePreference(
                preference = authMode,
                title = stringResource(MR.strings.host_auth),
                subtitle = stringResource(MR.strings.host_auth_sub),
                choices = (AuthMode.entries - AuthMode.UNKNOWN__).associateWith {
                    when (it) {
                        AuthMode.NONE -> stringResource(MR.strings.no_auth)
                        AuthMode.BASIC_AUTH -> stringResource(MR.strings.basic_auth)
                        AuthMode.SIMPLE_LOGIN -> stringResource(MR.strings.simple_auth)
                        AuthMode.UI_LOGIN -> stringResource(MR.strings.ui_login)
                        AuthMode.UNKNOWN__ -> ""
                    }
                }.toImmutableMap(),
                changeListener = serverSettingChanged,
            )
        }
        item {
            EditTextPreference(
                preference = authUsername,
                title = stringResource(MR.strings.host_auth_username),
                changeListener = serverSettingChanged,
                enabled = authEnabledValue,
            )
        }
        item {
            EditTextPreference(
                preference = authPassword,
                title = stringResource(MR.strings.host_auth_password),
                changeListener = serverSettingChanged,
                visualTransformation = PasswordVisualTransformation(),
                enabled = authEnabledValue,
            )
        }
    }
    item {
        Divider()
    }
}
