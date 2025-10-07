/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.core.lang.launchIO
import ca.gosyer.jui.domain.server.model.Auth
import ca.gosyer.jui.domain.server.model.Proxy
import ca.gosyer.jui.domain.server.service.ServerHostPreferences
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.settings.interactor.GetSettings
import ca.gosyer.jui.domain.settings.interactor.SetSettings
import ca.gosyer.jui.domain.settings.model.SetSettingsInput
import ca.gosyer.jui.domain.settings.model.Settings
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.ChoicePreference
import ca.gosyer.jui.ui.base.prefs.EditTextPreference
import ca.gosyer.jui.ui.base.prefs.PreferenceRow
import ca.gosyer.jui.ui.base.prefs.SwitchPreference
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.ui.viewModel
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.keyboardHandler
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.jui.uicore.prefs.asStateIn
import ca.gosyer.jui.uicore.prefs.asStringStateIn
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.listItems
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char
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
            authPassword = connectionVM.authPassword,
            serverSettings = connectionVM.serverSettings.collectAsState().value,
            hosted = connectionVM.host.collectAsState().value,
        )
    }
}

expect class SettingsServerHostViewModel : ViewModel

@Composable
expect fun getServerHostItems(viewModel: @Composable () -> SettingsServerHostViewModel): LazyListScope.() -> Unit

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class ServerSettingMutableStateFlow<T>(
    parent: StateFlow<Settings>,
    getSetting: (Settings) -> T,
    private val setSetting: (T) -> Unit,
    scope: CoroutineScope,
    private val state: MutableStateFlow<T> = MutableStateFlow(getSetting(parent.value)),
) : MutableStateFlow<T> by state {
    init {
        parent
            .onEach { state.value = getSetting(it) }
            .launchIn(scope)
    }

    override var value: T
        get() = state.value
        set(value) {
            setSetting(value)
        }
}

@Stable
class ServerSettings(
    private val getSettings: GetSettings,
    private val setSettings: SetSettings,
    private val scope: CoroutineScope,
    initial: Settings,
    private val onError: (String) -> Unit,
) {
    val settings = MutableStateFlow(initial)

    val autoDownloadNewChapters = getServerFlow(
        getSetting = { it.autoDownloadNewChapters },
        getInput = { SetSettingsInput(autoDownloadNewChapters = it) },
    )
    val autoDownloadNewChaptersLimit = getServerFlow(
        getSetting = { it.autoDownloadNewChaptersLimit.toString() },
        getInput = { SetSettingsInput(autoDownloadNewChaptersLimit = it.toIntOrNull()) },
    )
    val backupInterval = getServerFlow(
        getSetting = { it.backupInterval.toString() },
        getInput = { SetSettingsInput(backupInterval = it.toIntOrNull()) },
    )
    val backupPath = getServerFlow(
        getSetting = { it.backupPath },
        getInput = { SetSettingsInput(backupPath = it) },
    )
    val backupTTL = getServerFlow(
        getSetting = { it.backupTTL.toString() },
        getInput = { SetSettingsInput(backupTTL = it.toIntOrNull()) },
    )
    val backupTime = getServerFlow(
        getSetting = { it.backupTime },
        getInput = { SetSettingsInput(backupTime = it) },
    )

    //    val basicAuthEnabled = getServerFlow(
//        getSetting = { it.basicAuthEnabled },
//        getInput = { SetSettingsInput(basicAuthEnabled = it) },
//    )
//    val basicAuthPassword = getServerFlow(
//        getSetting = { it.basicAuthPassword },
//        getInput = { SetSettingsInput(basicAuthPassword = it) },
//    )
//    val basicAuthUsername = getServerFlow(
//        getSetting = { it.basicAuthUsername },
//        getInput = { SetSettingsInput(basicAuthUsername = it) },
//    )
    val debugLogsEnabled = getServerFlow(
        getSetting = { it.debugLogsEnabled },
        getInput = { SetSettingsInput(debugLogsEnabled = it) },
    )
    val downloadAsCbz = getServerFlow(
        getSetting = { it.downloadAsCbz },
        getInput = { SetSettingsInput(downloadAsCbz = it) },
    )
    val downloadsPath = getServerFlow(
        getSetting = { it.downloadsPath },
        getInput = { SetSettingsInput(downloadsPath = it) },
    )
    val electronPath = getServerFlow(
        getSetting = { it.electronPath },
        getInput = { SetSettingsInput(electronPath = it) },
    )
    val excludeCompleted = getServerFlow(
        getSetting = { it.excludeCompleted },
        getInput = { SetSettingsInput(excludeCompleted = it) },
    )
    val excludeEntryWithUnreadChapters = getServerFlow(
        getSetting = { it.excludeEntryWithUnreadChapters },
        getInput = { SetSettingsInput(excludeEntryWithUnreadChapters = it) },
    )
    val excludeNotStarted = getServerFlow(
        getSetting = { it.excludeNotStarted },
        getInput = { SetSettingsInput(excludeNotStarted = it) },
    )
    val excludeUnreadChapters = getServerFlow(
        getSetting = { it.excludeUnreadChapters },
        getInput = { SetSettingsInput(excludeUnreadChapters = it) },
    )
    val extensionRepos = getServerFlow(
        getSetting = { it.extensionRepos },
        getInput = { SetSettingsInput(extensionRepos = it) },
    )
    val flareSolverrEnabled = getServerFlow(
        getSetting = { it.flareSolverrEnabled },
        getInput = { SetSettingsInput(flareSolverrEnabled = it) },
    )
    val flareSolverrSessionName = getServerFlow(
        getSetting = { it.flareSolverrSessionName },
        getInput = { SetSettingsInput(flareSolverrSessionName = it) },
    )
    val flareSolverrSessionTtl = getServerFlow(
        getSetting = { it.flareSolverrSessionTtl.toString() },
        getInput = { SetSettingsInput(flareSolverrSessionTtl = it.toIntOrNull()) },
    )
    val flareSolverrTimeout = getServerFlow(
        getSetting = { it.flareSolverrTimeout.toString() },
        getInput = { SetSettingsInput(flareSolverrTimeout = it.toIntOrNull()) },
    )
    val flareSolverrUrl = getServerFlow(
        getSetting = { it.flareSolverrUrl },
        getInput = { SetSettingsInput(flareSolverrUrl = it) },
    )
    val globalUpdateInterval = getServerFlow(
        getSetting = { it.globalUpdateInterval.toString() },
        getInput = { SetSettingsInput(globalUpdateInterval = it.toDoubleOrNull()?.takeIf { it !in 0.01..5.99 }) },
    )

    //    val gqlDebugLogsEnabled = getServerFlow(
//        getSetting = { it.gqlDebugLogsEnabled },
//        getInput = { SetSettingsInput(gqlDebugLogsEnabled = it) },
//    )
    val initialOpenInBrowserEnabled = getServerFlow(
        getSetting = { it.initialOpenInBrowserEnabled },
        getInput = { SetSettingsInput(initialOpenInBrowserEnabled = it) },
    )
    val ip = getServerFlow(
        getSetting = { it.ip },
        getInput = { SetSettingsInput(ip = it) },
    )
    val localSourcePath = getServerFlow(
        getSetting = { it.localSourcePath },
        getInput = { SetSettingsInput(localSourcePath = it) },
    )
    val maxSourcesInParallel = getServerFlow(
        getSetting = { it.maxSourcesInParallel.toString() },
        getInput = { SetSettingsInput(maxSourcesInParallel = it.toIntOrNull()) },
    )
    val port = getServerFlow(
        getSetting = { it.port.toString() },
        getInput = { SetSettingsInput(port = it.toIntOrNull()) },
    )
    val socksProxyEnabled = getServerFlow(
        getSetting = { it.socksProxyEnabled },
        getInput = { SetSettingsInput(socksProxyEnabled = it) },
    )
    val socksProxyHost = getServerFlow(
        getSetting = { it.socksProxyHost },
        getInput = { SetSettingsInput(socksProxyHost = it) },
    )
    val socksProxyPassword = getServerFlow(
        getSetting = { it.socksProxyPassword },
        getInput = { SetSettingsInput(socksProxyPassword = it) },
    )
    val socksProxyPort = getServerFlow(
        getSetting = { it.socksProxyPort },
        getInput = { SetSettingsInput(socksProxyPort = it) },
    )
    val socksProxyUsername = getServerFlow(
        getSetting = { it.socksProxyUsername },
        getInput = { SetSettingsInput(socksProxyUsername = it) },
    )
    val socksProxyVersion = getServerFlow(
        getSetting = { it.socksProxyVersion },
        getInput = { SetSettingsInput(socksProxyVersion = it) },
    )
    val systemTrayEnabled = getServerFlow(
        getSetting = { it.systemTrayEnabled },
        getInput = { SetSettingsInput(systemTrayEnabled = it) },
    )
    val updateMangas = getServerFlow(
        getSetting = { it.updateMangas },
        getInput = { SetSettingsInput(updateMangas = it) },
    )
    val webUIChannel = getServerFlow(
        getSetting = { it.webUIChannel },
        getInput = { SetSettingsInput(webUIChannel = it) },
    )
    val webUIFlavor = getServerFlow(
        getSetting = { it.webUIFlavor },
        getInput = { SetSettingsInput(webUIFlavor = it) },
    )
    val webUIInterface = getServerFlow(
        getSetting = { it.webUIInterface },
        getInput = { SetSettingsInput(webUIInterface = it) },
    )
    val webUIUpdateCheckInterval = getServerFlow(
        getSetting = { it.webUIUpdateCheckInterval },
        getInput = { SetSettingsInput(webUIUpdateCheckInterval = it) },
    )

    private fun <T> getServerFlow(
        getSetting: (Settings) -> T,
        getInput: (T) -> SetSettingsInput,
    ): MutableStateFlow<T> =
        ServerSettingMutableStateFlow(
            parent = settings,
            getSetting = getSetting,
            setSetting = {
                scope.launch {
                    val input = getInput(it)
                    setSettings.await(
                        input,
                        onError = { onError(it.message.orEmpty()) },
                    )
                    val response = getSettings.await(onError = { onError(it.message.orEmpty()) })
                    if (response != null) {
                        settings.value = response
                    }
                }
            },
            scope = scope,
        )
}

@Inject
class SettingsServerViewModel(
    private val getSettings: GetSettings,
    private val setSettings: SetSettings,
    serverPreferences: ServerPreferences,
    serverHostPreferences: ServerHostPreferences,
    contextWrapper: ContextWrapper,
) : ViewModel(contextWrapper) {
    val serverUrl = serverPreferences.server().asStateIn(scope)
    val serverPort = serverPreferences.port().asStringStateIn(scope)
    val serverPathPrefix = serverPreferences.pathPrefix().asStateIn(scope)

    val proxy = serverPreferences.proxy().asStateIn(scope)

    val host = serverHostPreferences.host().asStateIn(scope)

    @Composable
    fun getProxyChoices(): ImmutableMap<Proxy, String> =
        persistentMapOf(
            Proxy.NO_PROXY to stringResource(MR.strings.no_proxy),
            Proxy.HTTP_PROXY to stringResource(MR.strings.http_proxy),
            Proxy.SOCKS_PROXY to stringResource(MR.strings.socks_proxy),
        )

    val httpHost = serverPreferences.proxyHttpHost().asStateIn(scope)
    val httpPort = serverPreferences.proxyHttpPort().asStringStateIn(scope)
    val socksHost = serverPreferences.proxySocksHost().asStateIn(scope)
    val socksPort = serverPreferences.proxySocksPort().asStringStateIn(scope)

    val auth = serverPreferences.auth().asStateIn(scope)

    @Composable
    fun getAuthChoices(): ImmutableMap<Auth, String> =
        persistentMapOf(
            Auth.NONE to stringResource(MR.strings.no_auth),
            Auth.BASIC to stringResource(MR.strings.basic_auth),
            Auth.DIGEST to stringResource(MR.strings.digest_auth),
        )

    val authUsername = serverPreferences.authUsername().asStateIn(scope)
    val authPassword = serverPreferences.authPassword().asStateIn(scope)

    private val _serverSettings = MutableStateFlow<ServerSettings?>(null)
    val serverSettings = _serverSettings.asStateFlow()

    init {
        scope.launchIO {
            val initialSettings = getSettings.await(onError = { toast(it.message.orEmpty()) })
            if (initialSettings != null) {
                _serverSettings.value = ServerSettings(
                    getSettings,
                    setSettings,
                    scope,
                    initialSettings,
                    onError = { toast(it) },
                )
            }
        }
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
    authPassword: PreferenceMutableStateFlow<String>,
    hosted: Boolean,
    serverSettings: ServerSettings?,
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal),
            ),
        ),
        topBar = {
            Toolbar(stringResource(MR.strings.settings_server_screen))
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
                serverHostItems()
                item {
                    EditTextPreference(
                        serverUrl,
                        stringResource(MR.strings.server_url),
                        subtitle = serverUrl.collectAsState().value,
                    )
                }
                item {
                    EditTextPreference(
                        serverPort,
                        stringResource(MR.strings.server_port),
                        subtitle = serverPort.collectAsState().value,
                    )
                }
                item {
                    EditTextPreference(
                        serverPathPrefix,
                        stringResource(MR.strings.server_path_prefix),
                        subtitle = stringResource(MR.strings.server_path_prefix_sub),
                    )
                }

                item {
                    PreferenceRow(
                        stringResource(MR.strings.server_preference_warning),
                        Icons.Rounded.Warning,
                        subtitle = stringResource(MR.strings.server_preference_warning_sub),
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
                                httpHost.collectAsState().value,
                            )
                        }
                        item {
                            EditTextPreference(
                                httpPort,
                                stringResource(MR.strings.http_port),
                                httpPort.collectAsState().value,
                            )
                        }
                    }

                    Proxy.SOCKS_PROXY -> {
                        item {
                            EditTextPreference(
                                socksHost,
                                stringResource(MR.strings.socks_proxy),
                                socksHost.collectAsState().value,
                            )
                        }
                        item {
                            EditTextPreference(
                                socksPort,
                                stringResource(MR.strings.socks_port),
                                socksPort.collectAsState().value,
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
                            visualTransformation = PasswordVisualTransformation(),
                        )
                    }
                }
                item {
                    Divider()
                }
                if (serverSettings != null) {
                    ServerSettingsItems(hosted, serverSettings)
                } else {
                    item {
                        Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
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
                                WindowInsetsSides.Bottom,
                            ),
                        ),
                    ),
            )
        }
    }
}

fun LazyListScope.ServerSettingsItems(
    hosted: Boolean,
    serverSettings: ServerSettings,
) {
    item {
        PreferenceRow(
            stringResource(MR.strings.host_settings),
            Icons.Rounded.Info,
            subtitle = stringResource(MR.strings.server_settings_sub),
        )
    }
    item {
        val ipValue by serverSettings.ip.collectAsState()
        EditTextPreference(
            preference = serverSettings.ip,
            title = stringResource(MR.strings.host_ip),
            subtitle = stringResource(MR.strings.host_ip_sub, ipValue),
            enabled = !hosted,
        )
    }
    item {
        val portValue by serverSettings.port.collectAsState()
        EditTextPreference(
            preference = serverSettings.port,
            title = stringResource(MR.strings.host_port),
            subtitle = stringResource(MR.strings.host_port_sub, portValue),
            enabled = !hosted,
        )
    }
    item {
        val dialog = rememberMaterialDialogState()
        PreferenceRow(
            stringResource(MR.strings.extension_repos),
            subtitle = stringResource(MR.strings.extension_repos_sub),
            onClick = dialog::show,
        )
        val repos by serverSettings.extensionRepos.collectAsState()
        ExtensionReposDialog(
            dialog,
            repos,
            onSetRepos = {
                serverSettings.extensionRepos.value = it
            },
        )
    }
    item {
        SwitchPreference(
            preference = serverSettings.socksProxyEnabled,
            title = stringResource(MR.strings.host_socks_enabled),
        )
    }
    item {
        val socksProxyEnabled by serverSettings.socksProxyEnabled.collectAsState()
        val proxyHost by serverSettings.socksProxyHost.collectAsState()
        EditTextPreference(
            preference = serverSettings.socksProxyHost,
            title = stringResource(MR.strings.host_socks_host),
            subtitle = stringResource(MR.strings.host_socks_host_sub, proxyHost),
            enabled = socksProxyEnabled,
        )
    }
    item {
        val socksProxyEnabled by serverSettings.socksProxyEnabled.collectAsState()
        val proxyPort by serverSettings.socksProxyPort.collectAsState()
        EditTextPreference(
            preference = serverSettings.socksProxyPort,
            title = stringResource(MR.strings.host_socks_port),
            subtitle = stringResource(MR.strings.host_socks_port_sub, proxyPort),
            enabled = socksProxyEnabled,
        )
    }
    item {
        val socksProxyEnabled by serverSettings.socksProxyEnabled.collectAsState()
        EditTextPreference(
            preference = serverSettings.socksProxyUsername,
            title = stringResource(MR.strings.host_socks_username),
            enabled = socksProxyEnabled,
        )
    }
    item {
        val socksProxyEnabled by serverSettings.socksProxyEnabled.collectAsState()
        EditTextPreference(
            preference = serverSettings.socksProxyPassword,
            title = stringResource(MR.strings.host_socks_password),
            visualTransformation = PasswordVisualTransformation(),
            enabled = socksProxyEnabled,
        )
    }
    item {
        val socksProxyEnabled by serverSettings.socksProxyEnabled.collectAsState()
        ChoicePreference(
            preference = serverSettings.socksProxyVersion,
            choices = mapOf(
                4 to "SOCKS4",
                5 to "SOCKS5",
            ).toImmutableMap(),
            title = stringResource(MR.strings.host_socks_version),
            enabled = socksProxyEnabled,
        )
    }
    item {
        EditTextPreference(
            preference = serverSettings.globalUpdateInterval,
            title = stringResource(MR.strings.global_update_interval),
            subtitle = stringResource(MR.strings.global_update_interval_sub),
        )
    }
    item {
        SwitchPreference(
            preference = serverSettings.updateMangas,
            title = stringResource(MR.strings.update_manga_info),
            subtitle = stringResource(MR.strings.update_manga_info_sub),
        )
    }
    item {
        SwitchPreference(
            preference = serverSettings.excludeCompleted,
            title = stringResource(MR.strings.exclude_completed),
            subtitle = stringResource(MR.strings.exclude_completed_sub),
        )
    }
    item {
        SwitchPreference(
            preference = serverSettings.excludeUnreadChapters,
            title = stringResource(MR.strings.exclude_unread),
            subtitle = stringResource(MR.strings.exclude_unread_sub),
        )
    }
    item {
        SwitchPreference(
            preference = serverSettings.excludeNotStarted,
            title = stringResource(MR.strings.exclude_not_started),
            subtitle = stringResource(MR.strings.exclude_not_started_sub),
        )
    }
    item {
        EditTextPreference(
            preference = serverSettings.maxSourcesInParallel,
            title = stringResource(MR.strings.max_sources_parallel),
            subtitle = stringResource(MR.strings.max_sources_parallel_sub),
        )
    }

    item {
        SwitchPreference(
            preference = serverSettings.downloadAsCbz,
            title = stringResource(MR.strings.host_download_as_cbz),
            subtitle = stringResource(MR.strings.host_download_as_cbz_sub),
        )
    }
    item {
        SwitchPreference(
            preference = serverSettings.autoDownloadNewChapters,
            title = stringResource(MR.strings.download_new_chapters),
        )
    }
    item {
        EditTextPreference(
            preference = serverSettings.autoDownloadNewChaptersLimit,
            title = stringResource(MR.strings.download_chapter_limit),
            subtitle = stringResource(MR.strings.download_chapter_limit_sub),
        )
    }
    item {
        SwitchPreference(
            preference = serverSettings.excludeEntryWithUnreadChapters,
            title = stringResource(MR.strings.ignore_unread_entries),
        )
    }

    item {
        SwitchPreference(
            preference = serverSettings.debugLogsEnabled,
            title = stringResource(MR.strings.host_debug_logging),
            subtitle = stringResource(MR.strings.host_debug_logging_sub),
        )
    }
//    item {
//        SwitchPreference(
//            preference = serverSettings.gqlDebugLogsEnabled,
//            title = stringResource(MR.strings.graphql_debug_logs),
//            subtitle = stringResource(MR.strings.graphql_debug_logs_sub),
//        )
//    }
    item {
        SwitchPreference(
            preference = serverSettings.systemTrayEnabled,
            title = stringResource(MR.strings.host_system_tray),
            subtitle = stringResource(MR.strings.host_system_tray_sub),
        )
    }
    item {
        // val webUIEnabledValue by serverSettings.webUIEnabled.collectAsState()
        SwitchPreference(
            preference = serverSettings.initialOpenInBrowserEnabled,
            title = stringResource(MR.strings.host_open_in_browser),
            subtitle = stringResource(MR.strings.host_open_in_browser_sub),
            enabled = !hosted, // webUIEnabledValue,
        )
    }

    item {
        EditTextPreference(
            preference = serverSettings.backupInterval,
            title = stringResource(MR.strings.backup_interval),
            subtitle = stringResource(MR.strings.backup_interval_sub),
        )
    }
    item {
        EditTextPreference(
            preference = serverSettings.backupTTL,
            title = stringResource(MR.strings.backup_ttl),
            subtitle = stringResource(MR.strings.backup_ttl_sub),
        )
    }
    item {
        val dialog = rememberMaterialDialogState()
        val backupTime by serverSettings.backupTime.collectAsState()
        PreferenceRow(
            title = stringResource(MR.strings.backup_time),
            subtitle = stringResource(MR.strings.backup_time_sub),
            onClick = dialog::show,
        )
        BackupTimeDialog(
            dialog,
            backupTime,
            onSetTime = {
                serverSettings.backupTime.value = it
            },
        )
    }

//    item {
//        SwitchPreference(
//            preference = serverSettings.basicAuthEnabled,
//            title = stringResource(MR.strings.basic_auth),
//            subtitle = stringResource(MR.strings.host_basic_auth_sub),
//            enabled = !hosted,
//        )
//    }
//
//    item {
//        val basicAuthEnabledValue by serverSettings.basicAuthEnabled.collectAsState()
//        EditTextPreference(
//            preference = serverSettings.basicAuthUsername,
//            title = stringResource(MR.strings.host_basic_auth_username),
//            enabled = basicAuthEnabledValue && !hosted,
//        )
//    }
//    item {
//        val basicAuthEnabledValue by serverSettings.basicAuthEnabled.collectAsState()
//        EditTextPreference(
//            preference = serverSettings.basicAuthPassword,
//            title = stringResource(MR.strings.host_basic_auth_password),
//            visualTransformation = PasswordVisualTransformation(),
//            enabled = basicAuthEnabledValue && !hosted,
//        )
//    }
    item {
        SwitchPreference(
            preference = serverSettings.flareSolverrEnabled,
            title = "FlareSolverr enabled",
            subtitle = "Use a FlareSolverr instance to bypass CloudFlare. Manual setup required",
        )
    }
    item {
        val flareSolverrEnabled by serverSettings.flareSolverrEnabled.collectAsState()
        EditTextPreference(
            preference = serverSettings.flareSolverrUrl,
            title = stringResource(MR.strings.flaresolverr_url),
            enabled = flareSolverrEnabled,
        )
    }
    item {
        val flareSolverrEnabled by serverSettings.flareSolverrEnabled.collectAsState()
        EditTextPreference(
            preference = serverSettings.flareSolverrTimeout,
            title = stringResource(MR.strings.flaresolverr_timeout),
            enabled = flareSolverrEnabled,
        )
    }
    item {
        val flareSolverrEnabled by serverSettings.flareSolverrEnabled.collectAsState()
        EditTextPreference(
            preference = serverSettings.flareSolverrSessionName,
            title = stringResource(MR.strings.flaresolverr_session_name),
            enabled = flareSolverrEnabled,
        )
    }
    item {
        val flareSolverrEnabled by serverSettings.flareSolverrEnabled.collectAsState()
        EditTextPreference(
            preference = serverSettings.flareSolverrSessionTtl,
            title = stringResource(MR.strings.flaresolverr_session_ttl),
            enabled = flareSolverrEnabled,
        )
    }

    item {
        Divider()
    }
}

private val repoRegex =
    (
        "https:\\/\\/(?>www\\.|raw\\.)?(github|githubusercontent)\\.com" +
            "\\/([^\\/]+)\\/([^\\/]+)(?>(?>\\/tree|\\/blob)?\\/([^\\/\\n]*))?(?>\\/([^\\/\\n]*\\.json)?)?"
    ).toRegex()

@Composable
fun ExtensionReposDialog(
    state: MaterialDialogState,
    extensionRepos: List<String>,
    onSetRepos: (List<String>) -> Unit,
) {
    val repos = remember(state.showing) {
        extensionRepos.toMutableStateList()
    }
    var newRepo by remember(state.showing) { mutableStateOf("") }
    MaterialDialog(
        state,
        properties = getMaterialDialogProperties(),
        buttons = {
            negativeButton(stringResource(MR.strings.action_cancel))
            positiveButton(stringResource(MR.strings.action_ok), onClick = { onSetRepos(repos.toList()) })
        },
    ) {
        title(stringResource(MR.strings.extension_repos))
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val repoMatches by derivedStateOf {
                newRepo.matches(repoRegex)
            }
            OutlinedTextField(
                value = newRepo,
                onValueChange = { newRepo = it },
                modifier = Modifier.weight(4f)
                    .keyboardHandler(
                        singleLine = true,
                        enterAction = {
                            if (repoMatches) {
                                repos.add(newRepo)
                                newRepo = ""
                            }
                        },
                    ),
                isError = newRepo.isNotBlank() && !repoMatches,
            )
            IconButton(
                onClick = {
                    repos.add(newRepo)
                    newRepo = ""
                },
                enabled = repoMatches,
                modifier = Modifier.weight(1f, fill = false),
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = stringResource(MR.strings.action_add),
                )
            }
        }

        listItems(
            modifier = Modifier.padding(bottom = 8.dp),
            list = repos,
            closeOnClick = false,
        ) { _, item ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    item,
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .weight(4f)
                        .wrapContentWidth(Alignment.Start),
                )
                IconButton(
                    onClick = { repos.remove(item) },
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = stringResource(MR.strings.action_delete),
                    )
                }
            }
        }
    }
}

val formatter = LocalTime.Format {
    hour()
    char(':')
    minute()
}

@Composable
fun BackupTimeDialog(
    state: MaterialDialogState,
    backupTime: String,
    onSetTime: (String) -> Unit,
) {
    val time = remember(state.showing) {
        LocalTime.parse(backupTime, formatter)
    }
    MaterialDialog(
        state,
        properties = getMaterialDialogProperties(),
        buttons = {
            negativeButton(stringResource(MR.strings.action_cancel))
            positiveButton(stringResource(MR.strings.action_ok))
        },
    ) {
        timepicker(
            time,
            title = stringResource(MR.strings.backup_time),
            onTimeChange = {
                onSetTime(formatter.format(it))
            },
        )
    }
}
