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
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
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
            socksProxyEnabled = serverVm.socksProxyEnabled,
            socksProxyHost = serverVm.socksProxyHost,
            socksProxyPort = serverVm.socksProxyPort,
            debugLogsEnabled = serverVm.debugLogsEnabled,
            systemTrayEnabled = serverVm.systemTrayEnabled,
            downloadPath = serverVm.downloadPath,
            downloadAsCbz = serverVm.downloadAsCbz,
            webUIEnabled = serverVm.webUIEnabled,
            openInBrowserEnabled = serverVm.openInBrowserEnabled,
            basicAuthEnabled = serverVm.basicAuthEnabled,
            basicAuthUsername = serverVm.basicAuthUsername,
            basicAuthPassword = serverVm.basicAuthPassword,
        )
    }
}

actual class SettingsServerHostViewModel @Inject constructor(
    serverPreferences: ServerPreferences,
    serverHostPreferences: ServerHostPreferences,
    private val serverService: ServerService,
    contextWrapper: ContextWrapper,
) : ViewModel(contextWrapper) {
    val host = serverHostPreferences.host().asStateIn(scope)
    val ip = serverHostPreferences.ip().asStateIn(scope)
    val port = serverHostPreferences.port().asStringStateIn(scope)

    // Proxy
    val socksProxyEnabled = serverHostPreferences.socksProxyEnabled().asStateIn(scope)
    val socksProxyHost = serverHostPreferences.socksProxyHost().asStateIn(scope)
    val socksProxyPort = serverHostPreferences.socksProxyPort().asStringStateIn(scope)

    // Misc
    val debugLogsEnabled = serverHostPreferences.debugLogsEnabled().asStateIn(scope)
    val systemTrayEnabled = serverHostPreferences.systemTrayEnabled().asStateIn(scope)

    // Downloader
    val downloadPath = serverHostPreferences.downloadPath().asStateIn(scope)
    val downloadAsCbz = serverHostPreferences.downloadAsCbz().asStateIn(scope)

    // WebUI
    val webUIEnabled = serverHostPreferences.webUIEnabled().asStateIn(scope)
    val openInBrowserEnabled = serverHostPreferences.openInBrowserEnabled().asStateIn(scope)

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
        combine(basicAuthEnabled, basicAuthUsername, basicAuthPassword) { enabled, username, password ->
            if (enabled) {
                auth.value = Auth.BASIC
                authUsername.value = username
                authPassword.value = password
            } else {
                auth.value = Auth.NONE
                authUsername.value = ""
                authPassword.value = ""
            }
        }.launchIn(scope)
    }
}

fun LazyListScope.ServerHostItems(
    hostValue: Boolean,
    basicAuthEnabledValue: Boolean,
    serverSettingChanged: () -> Unit,
    host: PreferenceMutableStateFlow<Boolean>,
    ip: PreferenceMutableStateFlow<String>,
    port: PreferenceMutableStateFlow<String>,
    socksProxyEnabled: PreferenceMutableStateFlow<Boolean>,
    socksProxyHost: PreferenceMutableStateFlow<String>,
    socksProxyPort: PreferenceMutableStateFlow<String>,
    debugLogsEnabled: PreferenceMutableStateFlow<Boolean>,
    systemTrayEnabled: PreferenceMutableStateFlow<Boolean>,
    downloadPath: PreferenceMutableStateFlow<String>,
    downloadAsCbz: PreferenceMutableStateFlow<Boolean>,
    webUIEnabled: PreferenceMutableStateFlow<Boolean>,
    openInBrowserEnabled: PreferenceMutableStateFlow<Boolean>,
    basicAuthEnabled: PreferenceMutableStateFlow<Boolean>,
    basicAuthUsername: PreferenceMutableStateFlow<String>,
    basicAuthPassword: PreferenceMutableStateFlow<String>,
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
            SwitchPreference(
                preference = socksProxyEnabled,
                title = stringResource(MR.strings.host_socks_enabled),
                changeListener = serverSettingChanged,
            )
        }
        item {
            val proxyHost by socksProxyHost.collectAsState()
            EditTextPreference(
                preference = socksProxyHost,
                title = stringResource(MR.strings.host_socks_host),
                subtitle = stringResource(MR.strings.host_socks_host_sub, proxyHost),
                changeListener = serverSettingChanged,
            )
        }
        item {
            val proxyPort by socksProxyPort.collectAsState()
            EditTextPreference(
                preference = socksProxyPort,
                title = stringResource(MR.strings.host_socks_port),
                subtitle = stringResource(MR.strings.host_socks_port_sub, proxyPort),
                changeListener = serverSettingChanged,
            )
        }
        item {
            SwitchPreference(
                preference = debugLogsEnabled,
                title = stringResource(MR.strings.host_debug_logging),
                subtitle = stringResource(MR.strings.host_debug_logging_sub),
                changeListener = serverSettingChanged,
            )
        }
        item {
            SwitchPreference(
                preference = systemTrayEnabled,
                title = stringResource(MR.strings.host_system_tray),
                subtitle = stringResource(MR.strings.host_system_tray_sub),
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
            SwitchPreference(
                preference = downloadAsCbz,
                title = stringResource(MR.strings.host_download_as_cbz),
                subtitle = stringResource(MR.strings.host_download_as_cbz_sub),
                changeListener = serverSettingChanged,
            )
        }
        item {
            SwitchPreference(
                preference = webUIEnabled,
                title = stringResource(MR.strings.host_webui),
                subtitle = stringResource(MR.strings.host_webui_sub),
                changeListener = serverSettingChanged,
            )
        }
        item {
            val webUIEnabledValue by webUIEnabled.collectAsState()
            SwitchPreference(
                preference = openInBrowserEnabled,
                title = stringResource(MR.strings.host_open_in_browser),
                subtitle = stringResource(MR.strings.host_open_in_browser_sub),
                changeListener = serverSettingChanged,
                enabled = webUIEnabledValue,
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
