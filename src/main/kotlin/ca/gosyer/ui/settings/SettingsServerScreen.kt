/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import ca.gosyer.data.server.ServerPreferences
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
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack
import javax.inject.Inject

class SettingsServerViewModel @Inject constructor(
    serverPreferences: ServerPreferences
) : ViewModel() {
    val host = serverPreferences.host().asStateIn(scope)
    val server = serverPreferences.server().asStateIn(scope)
    val port = serverPreferences.port().asStringStateIn(scope)

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
}

@Composable
fun SettingsServerScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsServerViewModel>()
    val proxy by vm.proxy.collectAsState()
    val auth by vm.auth.collectAsState()
    Column {
        Toolbar(stringResource("settings_server_screen"), navController, true)
        SwitchPreference(preference = vm.host, title = stringResource("host_server"))
        LazyColumn {
            item {
                EditTextPreference(vm.server, stringResource("server_url"), subtitle = vm.server.collectAsState().value)
            }
            item {
                EditTextPreference(vm.port, stringResource("server_port"), subtitle = vm.port.collectAsState().value)
            }

            item {
                PreferenceRow(
                    stringResource("server_preference_warning"),
                    Icons.Default.Info,
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
