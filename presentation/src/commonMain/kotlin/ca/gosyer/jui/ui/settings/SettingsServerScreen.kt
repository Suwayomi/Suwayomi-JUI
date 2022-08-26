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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import ca.gosyer.jui.domain.server.model.Auth
import ca.gosyer.jui.domain.server.model.Proxy
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.ChoicePreference
import ca.gosyer.jui.ui.base.prefs.EditTextPreference
import ca.gosyer.jui.ui.base.prefs.PreferenceRow
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.ui.viewModel
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.insets.navigationBars
import ca.gosyer.jui.uicore.insets.statusBars
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.jui.uicore.prefs.asStateIn
import ca.gosyer.jui.uicore.prefs.asStringStateIn
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.tatarka.inject.annotations.Inject

class SettingsServerScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val connectionVM = viewModel { settingsServerViewModel() }
        SettingsServerScreenContent(
            serverHostItems = getServerHostItems { viewModel { settingsServerHostViewModel() } },
            proxyValue = connectionVM.proxy.collectAsState().value,
            authValue = connectionVM.auth.collectAsState().value,
            serverUrl = connectionVM.serverUrl,
            serverPort = connectionVM.serverPort,
            serverPathPrefix = connectionVM.serverPathPrefix,
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

expect class SettingsServerHostViewModel : ViewModel

expect fun getServerHostItems(viewModel: @Composable () -> SettingsServerHostViewModel): LazyListScope.() -> Unit

class SettingsServerViewModel @Inject constructor(
    serverPreferences: ServerPreferences,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
    val serverUrl = serverPreferences.server().asStateIn(scope)
    val serverPort = serverPreferences.port().asStringStateIn(scope)
    val serverPathPrefix = serverPreferences.pathPrefix().asStateIn(scope)

    val proxy = serverPreferences.proxy().asStateIn(scope)

    @Composable
    fun getProxyChoices(): ImmutableMap<Proxy, String> = persistentMapOf(
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
    fun getAuthChoices(): ImmutableMap<Auth, String> = persistentMapOf(
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
}

@Composable
fun SettingsServerScreenContent(
    serverHostItems: LazyListScope.() -> Unit,
    proxyValue: Proxy,
    authValue: Auth,
    serverUrl: PreferenceMutableStateFlow<String>,
    serverPort: PreferenceMutableStateFlow<String>,
    serverPathPrefix: PreferenceMutableStateFlow<String>,
    proxy: PreferenceMutableStateFlow<Proxy>,
    proxyChoices: ImmutableMap<Proxy, String>,
    httpHost: PreferenceMutableStateFlow<String>,
    httpPort: PreferenceMutableStateFlow<String>,
    socksHost: PreferenceMutableStateFlow<String>,
    socksPort: PreferenceMutableStateFlow<String>,
    auth: PreferenceMutableStateFlow<Auth>,
    authChoices: ImmutableMap<Auth, String>,
    authUsername: PreferenceMutableStateFlow<String>,
    authPassword: PreferenceMutableStateFlow<String>
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
            )
        ),
        topBar = {
            Toolbar(stringResource(MR.strings.settings_server_screen))
        }
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                contentPadding = WindowInsets.bottomNav.add(
                    WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom
                    )
                ).asPaddingValues()
            ) {
                serverHostItems()
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
                    EditTextPreference(
                        serverPathPrefix,
                        stringResource(MR.strings.server_path_prefix),
                        subtitle = stringResource(MR.strings.server_path_prefix_sub)
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
                    .scrollbarPadding()
                    .windowInsetsPadding(
                        WindowInsets.bottomNav.add(
                            WindowInsets.navigationBars.only(
                                WindowInsetsSides.Bottom
                            )
                        )
                    )
            )
        }
    }
}
