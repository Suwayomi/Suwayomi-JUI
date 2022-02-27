/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
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
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.ui.base.prefs.ChoicePreference
import ca.gosyer.ui.base.prefs.EditTextPreference
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.uicore.prefs.asStateIn
import ca.gosyer.uicore.prefs.asStringStateIn
import ca.gosyer.uicore.resources.stringResource
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import me.tatarka.inject.annotations.Inject

class SettingsServerScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val connectionVM = viewModel<SettingsServerViewModel>()
        val serverVm = viewModel<SettingsServerHostViewModel>()
        SettingsServerScreenContent(
            hostValue = serverVm.host.collectAsState().value,
            basicAuthEnabledValue = serverVm.basicAuthEnabled.collectAsState().value,
            proxyValue = connectionVM.proxy.collectAsState().value,
            authValue = connectionVM.auth.collectAsState().value,
            restartServer = serverVm::restartServer,
            serverSettingChanged = serverVm::serverSettingChanged,
            host = serverVm.host,
            ip = serverVm.ip,
            port = serverVm.port,
            socksProxyEnabled = serverVm.socksProxyEnabled,
            socksProxyHost = serverVm.socksProxyHost,
            socksProxyPort = serverVm.socksProxyPort,
            debugLogsEnabled = serverVm.debugLogsEnabled,
            systemTrayEnabled = serverVm.systemTrayEnabled,
            webUIEnabled = serverVm.webUIEnabled,
            openInBrowserEnabled = serverVm.openInBrowserEnabled,
            basicAuthEnabled = serverVm.basicAuthEnabled,
            basicAuthUsername = serverVm.basicAuthUsername,
            basicAuthPassword = serverVm.basicAuthPassword,
            serverUrl = connectionVM.serverUrl,
            serverPort = connectionVM.serverPort,
            proxy = connectionVM.proxy,
            proxyChoices = connectionVM.getProxyChoices(),
            httpHost = connectionVM.httpHost,
            httpPort = connectionVM.httpPort,
            socksHost = connectionVM.socksHost,
            socksPort = connectionVM.socksPort,
            auth = connectionVM.auth,
            authChoices = connectionVM.getAuthChoices(),
            authUsername = connectionVM.authUsername,
            authPassword = connectionVM.authPassword
        )
    }
}

class SettingsServerViewModel @Inject constructor(
    serverPreferences: ServerPreferences,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
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

    private companion object : CKLogger({})
}

class SettingsServerHostViewModel @Inject constructor(
    serverPreferences: ServerPreferences,
    serverHostPreferences: ServerHostPreferences,
    private val serverService: ServerService,
    contextWrapper: ContextWrapper
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
            serverService.restartServer()
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
    private companion object : CKLogger({})
}

@Composable
fun SettingsServerScreenContent(
    hostValue: Boolean,
    basicAuthEnabledValue: Boolean,
    proxyValue: Proxy,
    authValue: Auth,
    restartServer: () -> Unit,
    serverSettingChanged: () -> Unit,
    host: PreferenceMutableStateFlow<Boolean>,
    ip: PreferenceMutableStateFlow<String>,
    port: PreferenceMutableStateFlow<String>,
    socksProxyEnabled: PreferenceMutableStateFlow<Boolean>,
    socksProxyHost: PreferenceMutableStateFlow<String>,
    socksProxyPort: PreferenceMutableStateFlow<String>,
    debugLogsEnabled: PreferenceMutableStateFlow<Boolean>,
    systemTrayEnabled: PreferenceMutableStateFlow<Boolean>,
    webUIEnabled: PreferenceMutableStateFlow<Boolean>,
    openInBrowserEnabled: PreferenceMutableStateFlow<Boolean>,
    basicAuthEnabled: PreferenceMutableStateFlow<Boolean>,
    basicAuthUsername: PreferenceMutableStateFlow<String>,
    basicAuthPassword: PreferenceMutableStateFlow<String>,
    serverUrl: PreferenceMutableStateFlow<String>,
    serverPort: PreferenceMutableStateFlow<String>,
    proxy: PreferenceMutableStateFlow<Proxy>,
    proxyChoices: Map<Proxy, String>,
    httpHost: PreferenceMutableStateFlow<String>,
    httpPort: PreferenceMutableStateFlow<String>,
    socksHost: PreferenceMutableStateFlow<String>,
    socksPort: PreferenceMutableStateFlow<String>,
    auth: PreferenceMutableStateFlow<Auth>,
    authChoices: Map<Auth, String>,
    authUsername: PreferenceMutableStateFlow<String>,
    authPassword: PreferenceMutableStateFlow<String>
) {
    DisposableEffect(Unit) {
        onDispose {
            restartServer()
        }
    }
    Scaffold(
        topBar = {
            Toolbar(stringResource(MR.strings.settings_server_screen))
        }
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
                item {
                    SwitchPreference(preference = host, title = stringResource(MR.strings.host_server))
                }
                if (hostValue) {
                    item {
                        PreferenceRow(
                            stringResource(MR.strings.host_settings),
                            Icons.Rounded.Info,
                            subtitle = stringResource(MR.strings.host_settings_sub)
                        )
                    }
                    item {
                        val ipValue by ip.collectAsState()
                        EditTextPreference(
                            preference = ip,
                            title = stringResource(MR.strings.host_ip),
                            subtitle = stringResource(MR.strings.host_ip_sub, ipValue),
                            changeListener = serverSettingChanged
                        )
                    }
                    item {
                        val portValue by port.collectAsState()
                        EditTextPreference(
                            preference = port,
                            title = stringResource(MR.strings.host_port),
                            subtitle = stringResource(MR.strings.host_port_sub, portValue),
                            changeListener = serverSettingChanged
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = socksProxyEnabled,
                            title = stringResource(MR.strings.host_socks_enabled),
                            changeListener = serverSettingChanged
                        )
                    }
                    item {
                        val proxyHost by socksProxyHost.collectAsState()
                        EditTextPreference(
                            preference = socksProxyHost,
                            title = stringResource(MR.strings.host_socks_host),
                            subtitle = stringResource(MR.strings.host_socks_host_sub, proxyHost),
                            changeListener = serverSettingChanged
                        )
                    }
                    item {
                        val proxyPort by socksProxyPort.collectAsState()
                        EditTextPreference(
                            preference = socksProxyPort,
                            title = stringResource(MR.strings.host_socks_port),
                            subtitle = stringResource(MR.strings.host_socks_port_sub, proxyPort),
                            changeListener = serverSettingChanged
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = debugLogsEnabled,
                            title = stringResource(MR.strings.host_debug_logging),
                            subtitle = stringResource(MR.strings.host_debug_logging_sub),
                            changeListener = serverSettingChanged
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = systemTrayEnabled,
                            title = stringResource(MR.strings.host_system_tray),
                            subtitle = stringResource(MR.strings.host_system_tray_sub),
                            changeListener = serverSettingChanged
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = webUIEnabled,
                            title = stringResource(MR.strings.host_webui),
                            subtitle = stringResource(MR.strings.host_webui_sub),
                            changeListener = serverSettingChanged
                        )
                    }
                    item {
                        val webUIEnabledValue by webUIEnabled.collectAsState()
                        SwitchPreference(
                            preference = openInBrowserEnabled,
                            title = stringResource(MR.strings.host_open_in_browser),
                            subtitle = stringResource(MR.strings.host_open_in_browser_sub),
                            changeListener = serverSettingChanged,
                            enabled = webUIEnabledValue
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = basicAuthEnabled,
                            title = stringResource(MR.strings.basic_auth),
                            subtitle = stringResource(MR.strings.host_basic_auth_sub),
                            changeListener = serverSettingChanged
                        )
                    }
                    item {
                        EditTextPreference(
                            preference = basicAuthUsername,
                            title = stringResource(MR.strings.host_basic_auth_username),
                            changeListener = serverSettingChanged,
                            enabled = basicAuthEnabledValue
                        )
                    }
                    item {
                        EditTextPreference(
                            preference = basicAuthPassword,
                            title = stringResource(MR.strings.host_basic_auth_password),
                            changeListener = serverSettingChanged,
                            visualTransformation = PasswordVisualTransformation(),
                            enabled = basicAuthEnabledValue
                        )
                    }
                }
                item {
                    Divider()
                }
                item {
                    EditTextPreference(
                        serverUrl,
                        stringResource(MR.strings.server_url),
                        subtitle = serverUrl.collectAsState().value
                    )
                }
                item {
                    EditTextPreference(
                        serverPort,
                        stringResource(MR.strings.server_port),
                        subtitle = serverPort.collectAsState().value
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
                    ChoicePreference(proxy, proxyChoices, stringResource(MR.strings.server_proxy))
                }
                when (proxyValue) {
                    Proxy.NO_PROXY -> Unit
                    Proxy.HTTP_PROXY -> {
                        item {
                            EditTextPreference(
                                httpHost,
                                stringResource(MR.strings.http_proxy),
                                httpHost.collectAsState().value
                            )
                        }
                        item {
                            EditTextPreference(
                                httpPort,
                                stringResource(MR.strings.http_port),
                                httpPort.collectAsState().value
                            )
                        }
                    }
                    Proxy.SOCKS_PROXY -> {
                        item {
                            EditTextPreference(
                                socksHost,
                                stringResource(MR.strings.socks_proxy),
                                socksHost.collectAsState().value
                            )
                        }
                        item {
                            EditTextPreference(
                                socksPort,
                                stringResource(MR.strings.socks_port),
                                socksPort.collectAsState().value
                            )
                        }
                    }
                }
                item {
                    ChoicePreference(auth, authChoices, stringResource(MR.strings.authentication))
                }
                if (authValue != Auth.NONE) {
                    item {
                        EditTextPreference(authUsername, stringResource(MR.strings.auth_username))
                    }
                    item {
                        EditTextPreference(
                            authPassword,
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
