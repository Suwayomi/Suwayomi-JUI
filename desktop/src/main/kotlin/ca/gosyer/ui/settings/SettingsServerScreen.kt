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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.server.ServerHostPreferences
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.ServerService
import ca.gosyer.data.server.model.Auth
import ca.gosyer.data.server.model.Proxy
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.components.MenuController
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.ChoicePreference
import ca.gosyer.ui.base.prefs.EditTextPreference
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.base.prefs.asStateIn
import ca.gosyer.ui.base.prefs.asStringStateIn
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import me.tatarka.inject.annotations.Inject

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

    // Authentication
    val basicAuthEnabled = serverHostPreferences.basicAuthEnabled().asStateIn(scope)
    val basicAuthUsername = serverHostPreferences.basicAuthUsername().asStateIn(scope)
    val basicAuthPassword = serverHostPreferences.basicAuthPassword().asStateIn(scope)

    // JUI connection
    val serverUrl = serverPreferences.server().asStateIn(scope)
    val serverPort = serverPreferences.port().asStringStateIn(scope)

    val proxy = serverPreferences.proxy().asStateIn(scope)

    @Composable
    fun getProxyChoices() = mapOf(
        Proxy.NO_PROXY to stringResource(MR.strings.no_proxy),
        Proxy.HTTP_PROXY to stringResource(MR.strings.http_proxy),
        Proxy.SOCKS_PROXY to stringResource(MR.strings.socks_proxy)
    )

    val httpHost = serverPreferences.proxyHttpHost().asStateIn(scope)
    val httpPort = serverPreferences.proxyHttpPort().asStringStateIn(scope)
    val socksHost = serverPreferences.proxySocksHost().asStateIn(scope)
    val socksPort = serverPreferences.proxySocksPort().asStringStateIn(scope)

    val auth = serverPreferences.auth().asStateIn(scope)

    @Composable
    fun getAuthChoices() = mapOf(
        Auth.NONE to stringResource(MR.strings.no_auth),
        Auth.BASIC to stringResource(MR.strings.basic_auth),
        Auth.DIGEST to stringResource(MR.strings.digest_auth)
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
    private companion object : CKLogger({})
}

@Composable
fun SettingsServerScreen(menuController: MenuController) {
    val vm = viewModel<SettingsServerViewModel>()
    val host by vm.host.collectAsState()
    val basicAuthEnabled by vm.basicAuthEnabled.collectAsState()
    val proxy by vm.proxy.collectAsState()
    val auth by vm.auth.collectAsState()
    DisposableEffect(Unit) {
        onDispose {
            vm.restartServer()
        }
    }
    Column {
        Toolbar(stringResource(MR.strings.settings_server_screen), menuController, true)
        Box {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
                item {
                    SwitchPreference(preference = vm.host, title = stringResource(MR.strings.host_server))
                }
                if (host) {
                    item {
                        PreferenceRow(
                            stringResource(MR.strings.host_settings),
                            Icons.Rounded.Info,
                            subtitle = stringResource(MR.strings.host_settings_sub)
                        )
                    }
                    item {
                        val ip by vm.ip.collectAsState()
                        EditTextPreference(
                            preference = vm.ip,
                            title = stringResource(MR.strings.host_ip),
                            subtitle = stringResource(MR.strings.host_ip_sub, ip),
                            changeListener = vm::serverSettingChanged
                        )
                    }
                    item {
                        val port by vm.port.collectAsState()
                        EditTextPreference(
                            preference = vm.port,
                            title = stringResource(MR.strings.host_port),
                            subtitle = stringResource(MR.strings.host_port_sub, port),
                            changeListener = vm::serverSettingChanged
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = vm.socksProxyEnabled,
                            title = stringResource(MR.strings.host_socks_enabled),
                            changeListener = vm::serverSettingChanged
                        )
                    }
                    item {
                        val proxyHost by vm.socksProxyHost.collectAsState()
                        EditTextPreference(
                            preference = vm.socksProxyHost,
                            title = stringResource(MR.strings.host_socks_host),
                            subtitle = stringResource(MR.strings.host_socks_host_sub, proxyHost),
                            changeListener = vm::serverSettingChanged
                        )
                    }
                    item {
                        val proxyPort by vm.socksProxyPort.collectAsState()
                        EditTextPreference(
                            preference = vm.socksProxyPort,
                            title = stringResource(MR.strings.host_socks_port),
                            subtitle = stringResource(MR.strings.host_socks_port_sub, proxyPort),
                            changeListener = vm::serverSettingChanged
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = vm.debugLogsEnabled,
                            title = stringResource(MR.strings.host_debug_logging),
                            subtitle = stringResource(MR.strings.host_debug_logging_sub),
                            changeListener = vm::serverSettingChanged
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = vm.systemTrayEnabled,
                            title = stringResource(MR.strings.host_system_tray),
                            subtitle = stringResource(MR.strings.host_system_tray_sub),
                            changeListener = vm::serverSettingChanged
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = vm.webUIEnabled,
                            title = stringResource(MR.strings.host_webui),
                            subtitle = stringResource(MR.strings.host_webui_sub),
                            changeListener = vm::serverSettingChanged
                        )
                    }
                    item {
                        val webUIEnabled by vm.webUIEnabled.collectAsState()
                        SwitchPreference(
                            preference = vm.openInBrowserEnabled,
                            title = stringResource(MR.strings.host_open_in_browser),
                            subtitle = stringResource(MR.strings.host_open_in_browser_sub),
                            changeListener = vm::serverSettingChanged,
                            enabled = webUIEnabled
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = vm.basicAuthEnabled,
                            title = stringResource(MR.strings.basic_auth),
                            subtitle = stringResource(MR.strings.host_basic_auth_sub),
                            changeListener = vm::serverSettingChanged
                        )
                    }
                    item {
                        EditTextPreference(
                            preference = vm.basicAuthUsername,
                            title = stringResource(MR.strings.host_basic_auth_username),
                            changeListener = vm::serverSettingChanged,
                            enabled = basicAuthEnabled
                        )
                    }
                    item {
                        EditTextPreference(
                            preference = vm.basicAuthPassword,
                            title = stringResource(MR.strings.host_basic_auth_password),
                            changeListener = vm::serverSettingChanged,
                            visualTransformation = PasswordVisualTransformation(),
                            enabled = basicAuthEnabled
                        )
                    }
                }
                item {
                    Divider()
                }
                item {
                    EditTextPreference(
                        vm.serverUrl,
                        stringResource(MR.strings.server_url),
                        subtitle = vm.serverUrl.collectAsState().value
                    )
                }
                item {
                    EditTextPreference(
                        vm.serverPort,
                        stringResource(MR.strings.server_port),
                        subtitle = vm.serverPort.collectAsState().value
                    )
                }

                item {
                    PreferenceRow(
                        stringResource(MR.strings.server_preference_warning),
                        Icons.Rounded.Warning,
                        subtitle = stringResource(MR.strings.server_preference_warning_sub)
                    )
                }
                item {
                    ChoicePreference(vm.proxy, vm.getProxyChoices(), stringResource(MR.strings.server_proxy))
                }
                when (proxy) {
                    Proxy.NO_PROXY -> Unit
                    Proxy.HTTP_PROXY -> {
                        item {
                            EditTextPreference(
                                vm.httpHost,
                                stringResource(MR.strings.http_proxy),
                                vm.httpHost.collectAsState().value
                            )
                        }
                        item {
                            EditTextPreference(
                                vm.httpPort,
                                stringResource(MR.strings.http_port),
                                vm.httpPort.collectAsState().value
                            )
                        }
                    }
                    Proxy.SOCKS_PROXY -> {
                        item {
                            EditTextPreference(
                                vm.socksHost,
                                stringResource(MR.strings.socks_proxy),
                                vm.socksHost.collectAsState().value
                            )
                        }
                        item {
                            EditTextPreference(
                                vm.socksPort,
                                stringResource(MR.strings.socks_port),
                                vm.socksPort.collectAsState().value
                            )
                        }
                    }
                }
                item {
                    ChoicePreference(vm.auth, vm.getAuthChoices(), stringResource(MR.strings.authentication))
                }
                if (auth != Auth.NONE) {
                    item {
                        EditTextPreference(vm.authUsername, stringResource(MR.strings.auth_username))
                    }
                    item {
                        EditTextPreference(
                            vm.authPassword,
                            stringResource(MR.strings.auth_password),
                            visualTransformation = PasswordVisualTransformation()
                        )
                    }
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
