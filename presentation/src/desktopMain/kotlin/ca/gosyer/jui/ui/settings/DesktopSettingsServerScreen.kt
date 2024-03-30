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
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.prefs.EditTextPreference
import ca.gosyer.jui.ui.base.prefs.PreferenceRow
import ca.gosyer.jui.ui.base.prefs.SwitchPreference
import ca.gosyer.jui.ui.util.system.folderPicker
import ca.gosyer.jui.uicore.prefs.asStateIn
import ca.gosyer.jui.uicore.prefs.asStringStateIn
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import me.tatarka.inject.annotations.Inject

@Composable
actual fun getServerHostItems(viewModel: @Composable () -> SettingsServerHostViewModel): LazyListScope.() -> Unit {
    val serverVm = viewModel()
    val hostValue by serverVm.host.collectAsState()
    val basicAuthEnabledValue by serverVm.basicAuthEnabled.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            serverVm.restartServer()
        }
    }

    return {
        ServerHostItems(
            hostValue = hostValue,
            basicAuthEnabledValue = basicAuthEnabledValue,
            serverSettingChanged = serverVm::serverSettingChanged,
            host = serverVm.host,
            ip = serverVm.ip,
            port = serverVm.port,
            downloadPath = serverVm.downloadPath,
            backupPath = serverVm.backupPath,
            localSourcePath = serverVm.localSourcePath,
            basicAuthEnabled = serverVm.basicAuthEnabled,
            basicAuthUsername = serverVm.basicAuthUsername,
            basicAuthPassword = serverVm.basicAuthPassword,
        )
    }
}

actual class SettingsServerHostViewModel
    @Inject
    constructor(
        serverPreferences: ServerPreferences,
        serverHostPreferences: ServerHostPreferences,
        private val serverService: ServerService,
        contextWrapper: ContextWrapper,
    ) : ViewModel(contextWrapper) {
        val host = serverHostPreferences.host().asStateIn(scope)
        val ip = serverHostPreferences.ip().asStateIn(scope)
        val port = serverHostPreferences.port().asStringStateIn(scope)

        // Downloader
        val downloadPath = serverHostPreferences.downloadPath().asStateIn(scope)

        // Backup
        val backupPath = serverHostPreferences.backupPath().asStateIn(scope)

        // LocalSource
        val localSourcePath = serverHostPreferences.localSourcePath().asStateIn(scope)

        // Authentication
        val basicAuthEnabled = serverHostPreferences.basicAuthEnabled().asStateIn(scope)
        val basicAuthUsername = serverHostPreferences.basicAuthUsername().asStateIn(scope)
        val basicAuthPassword = serverHostPreferences.basicAuthPassword().asStateIn(scope)

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
            combine(host, basicAuthEnabled, basicAuthUsername, basicAuthPassword) { host, enabled, username, password ->
                if (host) {
                    if (enabled) {
                        auth.value = Auth.BASIC
                        authUsername.value = username
                        authPassword.value = password
                    } else {
                        auth.value = Auth.NONE
                        authUsername.value = ""
                        authPassword.value = ""
                    }
                }
            }.launchIn(scope)
        }
    }

fun LazyListScope.ServerHostItems(
    hostValue: Boolean,
    basicAuthEnabledValue: Boolean,
    serverSettingChanged: () -> Unit,
    host: MutableStateFlow<Boolean>,
    ip: MutableStateFlow<String>,
    port: MutableStateFlow<String>,
    downloadPath: MutableStateFlow<String>,
    backupPath: MutableStateFlow<String>,
    localSourcePath: MutableStateFlow<String>,
    basicAuthEnabled: MutableStateFlow<Boolean>,
    basicAuthUsername: MutableStateFlow<String>,
    basicAuthPassword: MutableStateFlow<String>,
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
            SwitchPreference(
                preference = basicAuthEnabled,
                title = stringResource(MR.strings.basic_auth),
                subtitle = stringResource(MR.strings.host_basic_auth_sub),
                changeListener = serverSettingChanged,
            )
        }
        item {
            EditTextPreference(
                preference = basicAuthUsername,
                title = stringResource(MR.strings.host_basic_auth_username),
                changeListener = serverSettingChanged,
                enabled = basicAuthEnabledValue,
            )
        }
        item {
            EditTextPreference(
                preference = basicAuthPassword,
                title = stringResource(MR.strings.host_basic_auth_password),
                changeListener = serverSettingChanged,
                visualTransformation = PasswordVisualTransformation(),
                enabled = basicAuthEnabledValue,
            )
        }
    }
    item {
        Divider()
    }
}
