/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import ca.gosyer.jui.data.reader.ReaderModePreferences
import ca.gosyer.jui.data.reader.ReaderPreferences
import ca.gosyer.jui.data.reader.model.Direction
import ca.gosyer.jui.data.reader.model.ImageScale
import ca.gosyer.jui.data.reader.model.NavigationMode
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.ChoicePreference
import ca.gosyer.jui.ui.base.prefs.ExpandablePreference
import ca.gosyer.jui.ui.base.prefs.SwitchPreference
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.jui.uicore.prefs.asStateIn
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import ca.gosyer.jui.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject

class SettingsReaderScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<SettingsReaderViewModel>()
        SettingsReaderScreenContent(
            modes = vm.modes.collectAsState().value.associateWith { it },
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
    val selectedMode = readerPreferences.mode().asStateIn(scope)

    private val _modeSettings = MutableStateFlow(emptyList<ReaderModePreference>())
    val modeSettings = _modeSettings.asStateFlow()

    init {
        modes.onEach { modes ->
            val modeSettings = _modeSettings.value
            val modesInSettings = modeSettings.map { it.mode }
            _modeSettings.value = modeSettings.filter { it.mode in modes } + modes.filter {
                it !in modesInSettings
            }.map {
                ReaderModePreference(scope, it, readerPreferences.getMode(it))
            }
        }.launchIn(scope)
    }

    fun getDirectionChoices() = Direction.values().associateWith { it.res.toPlatformString() }

    fun getPaddingChoices() = mapOf(
        0 to MR.strings.page_padding_none.toPlatformString(),
        8 to "8 Dp",
        16 to "16 Dp",
        32 to "32 Dp"
    )

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
    }

    fun getImageScaleChoices() = ImageScale.values().associateWith { it.res.toPlatformString() }

    fun getNavigationModeChoices() = NavigationMode.values().associateWith { it.res.toPlatformString() }
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
    modes: Map<String, String>,
    selectedMode: PreferenceMutableStateFlow<String>,
    modeSettings: List<ReaderModePreference>,
    directionChoices: Map<Direction, String>,
    paddingChoices: Map<Int, String>,
    getMaxSizeChoices: (Direction) -> Map<Int, String>,
    imageScaleChoices: Map<ImageScale, String>,
    navigationModeChoices: Map<NavigationMode, String>
) {
    Scaffold(
        topBar = {
            Toolbar(stringResource(MR.strings.settings_reader_screen))
        }
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
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
                modeSettings.fastForEach {
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
            )
        }
    }
}
