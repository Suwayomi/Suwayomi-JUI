/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.util.fastForEach
import ca.gosyer.data.reader.ReaderModePreferences
import ca.gosyer.data.reader.ReaderPreferences
import ca.gosyer.data.reader.model.Direction
import ca.gosyer.data.reader.model.ImageScale
import ca.gosyer.data.reader.model.NavigationMode
import ca.gosyer.data.translation.XmlResourceBundle
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.ChoicePreference
import ca.gosyer.ui.base.prefs.ExpandablePreference
import ca.gosyer.ui.base.prefs.PreferenceMutableStateFlow
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.base.prefs.asStateIn
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class SettingsReaderViewModel @Inject constructor(
    private val resources: XmlResourceBundle,
    readerPreferences: ReaderPreferences
) : ViewModel() {
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

    fun getDirectionChoices() = Direction.values().associate { it to resources.getStringA(it.res) }

    fun getPaddingChoices() = mapOf(
        0 to resources.getStringA("page_padding_none"),
        8 to "8 Dp",
        16 to "16 Dp",
        32 to "32 Dp"
    )

    fun getMaxSizeChoices(direction: Direction) = if (direction == Direction.Right || direction == Direction.Left) {
        mapOf(
            0 to resources.getStringA("max_size_unrestricted"),
            700 to "700 Dp",
            900 to "900 Dp",
            1100 to "1100 Dp"
        )
    } else {
        mapOf(
            0 to resources.getStringA("max_size_unrestricted"),
            500 to "500 Dp",
            700 to "700 Dp",
            900 to "900 Dp"
        )
    }

    fun getImageScaleChoices() = ImageScale.values().associate { it to resources.getStringA(it.res) }

    fun getNavigationModeChoices() = NavigationMode.values().associate { it to resources.getStringA(it.res) }
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
fun SettingsReaderScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsReaderViewModel>()
    val modeSettings by vm.modeSettings.collectAsState()
    Column {
        Toolbar(stringResource("settings_reader"), navController, true)
        LazyColumn {
            item {
                ChoicePreference(
                    vm.selectedMode,
                    vm.modes.collectAsState().value.associateWith { it },
                    stringResource("reader_mode")
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
                            vm.getDirectionChoices(),
                            stringResource("direction"),
                            enabled = !it.defaultMode
                        )
                        SwitchPreference(
                            it.continuous,
                            stringResource("continuous"),
                            stringResource("continuous_sub"),
                            enabled = !it.defaultMode
                        )
                        val continuous by it.continuous.collectAsState()
                        if (continuous) {
                            ChoicePreference(
                                it.padding,
                                vm.getPaddingChoices(),
                                stringResource("page_padding")
                            )
                            val direction by it.direction.collectAsState()
                            val (title, subtitle) = if (direction == Direction.Up || direction == Direction.Down) {
                                stringResource("force_fit_width") to stringResource("force_fit_width_sub")
                            } else {
                                stringResource("force_fit_height") to stringResource("force_fit_height_sub")
                            }
                            SwitchPreference(
                                it.fitSize,
                                title,
                                subtitle
                            )
                            val maxSize by it.maxSize.collectAsState()
                            val (maxSizeTitle, maxSizeSubtitle) = if (direction == Direction.Up || direction == Direction.Down) {
                                stringResource("max_width") to stringResource("max_width_sub", maxSize.toString())
                            } else {
                                stringResource("max_height") to stringResource("max_height_sub", maxSize.toString())
                            }
                            ChoicePreference(
                                it.maxSize,
                                vm.getMaxSizeChoices(direction),
                                maxSizeTitle,
                                maxSizeSubtitle
                            )
                        } else {
                            ChoicePreference(
                                it.imageScale,
                                vm.getImageScaleChoices(),
                                stringResource("image_scale")
                            )
                        }
                        ChoicePreference(
                            it.navigationMode,
                            vm.getNavigationModeChoices(),
                            stringResource("navigation_mode")
                        )
                    }
                }
                item {
                    Divider()
                }
            }
        }
    }
}
