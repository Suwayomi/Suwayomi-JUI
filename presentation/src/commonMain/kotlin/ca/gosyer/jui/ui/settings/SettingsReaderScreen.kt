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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import ca.gosyer.jui.domain.reader.model.Direction
import ca.gosyer.jui.domain.reader.model.ImageScale
import ca.gosyer.jui.domain.reader.model.NavigationMode
import ca.gosyer.jui.domain.reader.service.ReaderModePreferences
import ca.gosyer.jui.domain.reader.service.ReaderPreferences
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.ChoicePreference
import ca.gosyer.jui.ui.base.prefs.ExpandablePreference
import ca.gosyer.jui.ui.base.prefs.SwitchPreference
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.ui.viewModel
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.insets.navigationBars
import ca.gosyer.jui.uicore.insets.statusBars
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.jui.uicore.prefs.asStateIn
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject

class SettingsReaderScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel { settingsReaderViewModel() }
        SettingsReaderScreenContent(
            modes = vm.modes.collectAsState().value,
            selectedMode = vm.selectedMode,
            modeSettings = vm.modeSettings.collectAsState().value,
            directionChoices = vm.getDirectionChoices(),
            paddingChoices = vm.getPaddingChoices(),
            getMaxSizeChoices = vm::getMaxSizeChoices,
            imageScaleChoices = vm.getImageScaleChoices(),
            navigationModeChoices = vm.getNavigationModeChoices()
        )
    }
}

class SettingsReaderViewModel @Inject constructor(
    readerPreferences: ReaderPreferences,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
    val modes = readerPreferences.modes().asStateFlow()
        .map {
            it.associateWith { it }
                .toImmutableMap()
        }
        .stateIn(scope, SharingStarted.Eagerly, persistentMapOf())
    val selectedMode = readerPreferences.mode().asStateIn(scope)

    private val _modeSettings = MutableStateFlow<ImmutableList<StableHolder<ReaderModePreference>>>(
        persistentListOf()
    )
    val modeSettings = _modeSettings.asStateFlow()

    init {
        modes.onEach { modes ->
            val modeSettings = _modeSettings.value
            val modesInSettings = modeSettings.map { it.item.mode }
            _modeSettings.value = modeSettings.filter { it.item.mode in modes }.toPersistentList() + modes.filter { (it) ->
                it !in modesInSettings
            }.map { (it) ->
                StableHolder(ReaderModePreference(scope, it, readerPreferences.getMode(it)))
            }
        }.launchIn(scope)
    }

    fun getDirectionChoices() = Direction.values().associateWith { it.res.toPlatformString() }
        .toImmutableMap()

    fun getPaddingChoices() = mapOf(
        0 to MR.strings.page_padding_none.toPlatformString(),
        8 to "8 Dp",
        16 to "16 Dp",
        32 to "32 Dp"
    ).toImmutableMap()

    fun getMaxSizeChoices(direction: Direction) = if (direction == Direction.Right || direction == Direction.Left) {
        mapOf(
            0 to MR.strings.max_size_unrestricted.toPlatformString(),
            700 to "700 Dp",
            900 to "900 Dp",
            1100 to "1100 Dp"
        )
    } else {
        mapOf(
            0 to MR.strings.max_size_unrestricted.toPlatformString(),
            500 to "500 Dp",
            700 to "700 Dp",
            900 to "900 Dp"
        )
    }.toImmutableMap()

    fun getImageScaleChoices() = ImageScale.values().associateWith { it.res.toPlatformString() }
        .toImmutableMap()

    fun getNavigationModeChoices() = NavigationMode.values().associateWith { it.res.toPlatformString() }
        .toImmutableMap()
}

data class ReaderModePreference(
    val scope: CoroutineScope,
    val mode: String,
    val defaultMode: Boolean,
    val continuous: PreferenceMutableStateFlow<Boolean>,
    val direction: PreferenceMutableStateFlow<Direction>,
    val padding: PreferenceMutableStateFlow<Int>,
    val imageScale: PreferenceMutableStateFlow<ImageScale>,
    val fitSize: PreferenceMutableStateFlow<Boolean>,
    val maxSize: PreferenceMutableStateFlow<Int>,
    val navigationMode: PreferenceMutableStateFlow<NavigationMode>
) {
    constructor(scope: CoroutineScope, mode: String, readerPreferences: ReaderModePreferences) :
        this(
            scope,
            mode,
            readerPreferences.default().get(),
            readerPreferences.continuous().asStateIn(scope),
            readerPreferences.direction().asStateIn(scope),
            readerPreferences.padding().asStateIn(scope),
            readerPreferences.imageScale().asStateIn(scope),
            readerPreferences.fitSize().asStateIn(scope),
            readerPreferences.maxSize().asStateIn(scope),
            readerPreferences.navigationMode().asStateIn(scope)
        )
}

@Composable
fun SettingsReaderScreenContent(
    modes: ImmutableMap<String, String>,
    selectedMode: PreferenceMutableStateFlow<String>,
    modeSettings: ImmutableList<StableHolder<ReaderModePreference>>,
    directionChoices: ImmutableMap<Direction, String>,
    paddingChoices: ImmutableMap<Int, String>,
    getMaxSizeChoices: (Direction) -> ImmutableMap<Int, String>,
    imageScaleChoices: ImmutableMap<ImageScale, String>,
    navigationModeChoices: ImmutableMap<NavigationMode, String>
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
            )
        ),
        topBar = {
            Toolbar(stringResource(MR.strings.settings_reader_screen))
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
                item {
                    ChoicePreference(
                        selectedMode,
                        modes,
                        stringResource(MR.strings.reader_mode)
                    )
                }
                item {
                    Divider()
                }
                modeSettings.fastForEach { (it) ->
                    item {
                        ExpandablePreference(it.mode) {
                            ChoicePreference(
                                it.direction,
                                directionChoices,
                                stringResource(MR.strings.direction),
                                enabled = !it.defaultMode
                            )
                            SwitchPreference(
                                it.continuous,
                                stringResource(MR.strings.continuous),
                                stringResource(MR.strings.continuous_sub),
                                enabled = !it.defaultMode
                            )
                            val continuous by it.continuous.collectAsState()
                            if (continuous) {
                                ChoicePreference(
                                    it.padding,
                                    paddingChoices,
                                    stringResource(MR.strings.page_padding)
                                )
                                val direction by it.direction.collectAsState()
                                val (title, subtitle) = if (direction == Direction.Up || direction == Direction.Down) {
                                    stringResource(MR.strings.force_fit_width) to stringResource(MR.strings.force_fit_width_sub)
                                } else {
                                    stringResource(MR.strings.force_fit_height) to stringResource(MR.strings.force_fit_height_sub)
                                }
                                SwitchPreference(
                                    it.fitSize,
                                    title,
                                    subtitle
                                )
                                val maxSize by it.maxSize.collectAsState()
                                val (maxSizeTitle, maxSizeSubtitle) = if (direction == Direction.Up || direction == Direction.Down) {
                                    stringResource(MR.strings.max_width) to stringResource(
                                        MR.strings.max_width_sub,
                                        maxSize
                                    )
                                } else {
                                    stringResource(MR.strings.max_height) to stringResource(
                                        MR.strings.max_height_sub,
                                        maxSize
                                    )
                                }
                                ChoicePreference(
                                    it.maxSize,
                                    getMaxSizeChoices(direction),
                                    maxSizeTitle,
                                    maxSizeSubtitle
                                )
                            } else {
                                ChoicePreference(
                                    it.imageScale,
                                    imageScaleChoices,
                                    stringResource(MR.strings.image_scale)
                                )
                            }
                            ChoicePreference(
                                it.navigationMode,
                                navigationModeChoices,
                                stringResource(MR.strings.navigation_mode)
                            )
                        }
                    }
                    item {
                        Divider()
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
