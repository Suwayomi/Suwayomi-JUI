/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import ca.gosyer.data.server.ServerHostPreferences
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.ServerService
import ca.gosyer.data.server.model.Auth
import ca.gosyer.data.server.model.Proxy
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.ChoicePreference
import ca.gosyer.ui.base.prefs.EditTextPreference
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.base.prefs.asStateIn
import ca.gosyer.ui.base.prefs.asStringStateIn
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Routes
import com.github.zsoltk.compose.router.BackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SettingsServerViewModel @Inject constructor(
    serverPreferences: ServerPreferences,
    serverHostPreferences: ServerHostPreferences,
    private val serverService: ServerService
) : ViewModel() {
    val host = serverPreferences.host().asStateIn(scope)
    val ip = serverHostPreferences.ip().asStateIn(scope)
    val port = serverHostPreferences.port().asStringStateIn(scope)

    // Proxy
    val socksProxyEnabled = serverHostPreferences.socksProxyEnabled().asStateIn(scope)
    val socksProxyHost = serverHostPreferences.socksProxyHost().asStateIn(scope)
    val socksProxyPort = serverHostPreferences.socksProxyPort().asStringStateIn(scope)

    // Misc
    val debugLogsEnabled = serverHostPreferences.debugLogsEnabled().asStateIn(scope)
    val systemTrayEnabled = serverHostPreferences.systemTrayEnabled().asStateIn(scope)

    // WebUI
    val webUIEnabled = serverHostPreferences.webUIEnabled().asStateIn(scope)
    val openInBrowserEnabled = serverHostPreferences.openInBrowserEnabled().asStateIn(scope)

    // JUI connection
    val serverUrl = serverPreferences.server().asStateIn(scope)
    val serverPort = serverPreferences.port().asStringStateIn(scope)

    val proxy = serverPreferences.proxy().asStateIn(scope)

    @Composable
    fun getProxyChoices() = mapOf(
        Proxy.NO_PROXY to stringResource("no_proxy"),
        Proxy.HTTP_PROXY to stringResource("http_proxy"),
        Proxy.SOCKS_PROXY to stringResource("socks_proxy")
    )

    val httpHost = serverPreferences.proxyHttpHost().asStateIn(scope)
    val httpPort = serverPreferences.proxyHttpPort().asStringStateIn(scope)
    val socksHost = serverPreferences.proxySocksHost().asStateIn(scope)
    val socksPort = serverPreferences.proxySocksPort().asStringStateIn(scope)

    val auth = serverPreferences.auth().asStateIn(scope)

    @Composable
    fun getAuthChoices() = mapOf(
        Auth.NONE to stringResource("no_auth"),
        Auth.BASIC to stringResource("basic_auth"),
        Auth.DIGEST to stringResource("digest_auth")
    )
    val authUsername = serverPreferences.authUsername().asStateIn(scope)
    val authPassword = serverPreferences.authPassword().asStateIn(scope)

    private val _serverSettingChanged = MutableStateFlow(false)
    val serverSettingChanged = _serverSettingChanged.asStateFlow()
    fun serverSettingChanged() {
        _serverSettingChanged.value = true
    }

    fun restartServer() {
        if (serverSettingChanged.value) {
            serverService.restartServer()
        }
    }
}

@Composable
fun SettingsServerScreen(navController: BackStack<Routes>) {
    val vm = viewModel<SettingsServerViewModel>()
    val host by vm.host.collectAsState()
    val proxy by vm.proxy.collectAsState()
    val auth by vm.auth.collectAsState()
    DisposableEffect(Unit) {
        onDispose {
            vm.restartServer()
        }
    }
    Column {
        Toolbar(stringResource("settings_server_screen"), navController, true)
        LazyColumn {
            item {
                SwitchPreference(preference = vm.host, title = stringResource("host_server"))
            }
            if (host) {
                item {
                    PreferenceRow(
                        stringResource("host_settings"),
                        Icons.Rounded.Info,
                        subtitle = stringResource("host_settings_sub")
                    )
                }
                item {
                    val ip by vm.ip.collectAsState()
                    EditTextPreference(
                        preference = vm.ip,
                        title = stringResource("host_ip"),
                        subtitle = stringResource("host_ip_sub", ip),
                        changeListener = vm::serverSettingChanged
                    )
                }
                item {
                    val port by vm.port.collectAsState()
                    EditTextPreference(
                        preference = vm.port,
                        title = stringResource("host_port"),
                        subtitle = stringResource("host_port_sub", port),
                        changeListener = vm::serverSettingChanged
                    )
                }
                item {
                    SwitchPreference(
                        preference = vm.socksProxyEnabled,
                        title = stringResource("host_socks_enabled"),
                        changeListener = vm::serverSettingChanged
                    )
                }
                item {
                    val proxyHost by vm.socksProxyHost.collectAsState()
                    EditTextPreference(
                        preference = vm.socksProxyHost,
                        title = stringResource("host_socks_host"),
                        subtitle = stringResource("host_socks_host_sub", proxyHost),
                        changeListener = vm::serverSettingChanged
                    )
                }
                item {
                    val proxyPort by vm.socksProxyPort.collectAsState()
                    EditTextPreference(
                        preference = vm.socksProxyPort,
                        title = stringResource("host_socks_port"),
                        subtitle = stringResource("host_socks_port_sub", proxyPort),
                        changeListener = vm::serverSettingChanged
                    )
                }
                item {
                    SwitchPreference(
                        preference = vm.debugLogsEnabled,
                        title = stringResource("host_debug_logging"),
                        subtitle = stringResource("host_debug_logging_sub"),
                        changeListener = vm::serverSettingChanged
                    )
                }
                item {
                    SwitchPreference(
                        preference = vm.systemTrayEnabled,
                        title = stringResource("host_system_tray"),
                        subtitle = stringResource("host_system_tray_sub"),
                        changeListener = vm::serverSettingChanged
                    )
                }
                item {
                    SwitchPreference(
                        preference = vm.webUIEnabled,
                        title = stringResource("host_webui"),
                        subtitle = stringResource("host_webui_sub"),
                        changeListener = vm::serverSettingChanged
                    )
                }
                item {
                    val webUIEnabled by vm.webUIEnabled.collectAsState()
                    SwitchPreference(
                        preference = vm.openInBrowserEnabled,
                        title = stringResource("host_open_in_browser"),
                        subtitle = stringResource("host_open_in_browser_sub"),
                        changeListener = vm::serverSettingChanged,
                        enabled = webUIEnabled
                    )
                }
            }
            item {
                Divider()
            }
            item {
                EditTextPreference(vm.serverUrl, stringResource("server_url"), subtitle = vm.serverUrl.collectAsState().value)
            }
            item {
                EditTextPreference(vm.serverPort, stringResource("server_port"), subtitle = vm.serverPort.collectAsState().value)
            }

            item {
                PreferenceRow(
                    stringResource("server_preference_warning"),
                    Icons.Rounded.Warning,
                    subtitle = stringResource("server_preference_warning_sub")
                )
            }
            item {
                ChoicePreference(vm.proxy, vm.getProxyChoices(), stringResource("server_proxy"))
            }
            when (proxy) {
                Proxy.NO_PROXY -> Unit
                Proxy.HTTP_PROXY -> {
                    item {
                        EditTextPreference(vm.httpHost, stringResource("http_proxy"), vm.httpHost.collectAsState().value)
                    }
                    item {
                        EditTextPreference(vm.httpPort, stringResource("http_port"), vm.httpPort.collectAsState().value)
                    }
                }
                Proxy.SOCKS_PROXY -> {
                    item {
                        EditTextPreference(vm.socksHost, stringResource("socks_proxy"), vm.socksHost.collectAsState().value)
                    }
                    item {
                        EditTextPreference(vm.socksPort, stringResource("socks_port"), vm.socksPort.collectAsState().value)
                    }
                }
            }
            item {
                ChoicePreference(vm.auth, vm.getAuthChoices(), stringResource("authentication"))
            }
            if (auth != Auth.NONE) {
                item {
                    EditTextPreference(vm.authUsername, stringResource("auth_username"))
                }
                item {
                    EditTextPreference(vm.authPassword, stringResource("auth_password"), visualTransformation = PasswordVisualTransformation())
                }
            }
        }
    }
}
