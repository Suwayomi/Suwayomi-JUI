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
import ca.gosyer.data.reader.ReaderModePreferences
import ca.gosyer.data.reader.ReaderPreferences
import ca.gosyer.data.reader.model.Direction
import ca.gosyer.data.reader.model.ImageScale
import ca.gosyer.data.reader.model.NavigationMode
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.ChoicePreference
import ca.gosyer.ui.base.prefs.ExpandablePreference
import ca.gosyer.ui.base.prefs.PreferenceMutableStateFlow
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.base.prefs.asStateIn
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
            _modeSettings.value = modeSettings.filter { it.mode in modes }
                .plus(
                    modes.filter {
                        it !in modesInSettings
                    }.map {
                        ReaderModePreference(scope, it, readerPreferences.getMode(it))
                    }
                )
        }.launchIn(scope)
    }

    fun getDirectionChoices() = Direction.values().associate { it to it.res }

    fun getPaddingChoices(continuous: Boolean) = if (continuous) {
        mapOf(
            0.0F to "None",
            0.5F to "0.5 Dp",
            1.0F to "1 Dp"
        )
    } else {
        mapOf(
            0.0F to "None",
            4.0F to "4 Dp",
            8.0F to "8 Dp"
        )
    }

    fun getImageScaleChoices() = ImageScale.values().associate { it to it.res }

    fun getNavigationModeChoices() = NavigationMode.values().associate { it to it.res }
}

data class ReaderModePreference(
    val scope: CoroutineScope,
    val mode: String,
    val defaultMode: Boolean,
    val continuous: PreferenceMutableStateFlow<Boolean>,
    val direction: PreferenceMutableStateFlow<Direction>,
    val padding: PreferenceMutableStateFlow<Float>,
    val imageScale: PreferenceMutableStateFlow<ImageScale>,
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
            readerPreferences.navigationMode().asStateIn(scope)
        )

    init {
        continuous
            .onEach {
                padding.value = 0.0F
            }
            .launchIn(scope)
    }
}

@Composable
fun SettingsReaderScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsReaderViewModel>()
    val modeSettings by vm.modeSettings.collectAsState()
    Column {
        Toolbar("Reader Settings", navController, true)
        LazyColumn {
            item {
                ChoicePreference(
                    vm.selectedMode,
                    vm.modes.collectAsState().value.associateWith { it },
                    "Reader Mode"
                )
            }
            item {
                Divider()
            }
            modeSettings.forEach {
                item {
                    ExpandablePreference(it.mode) {
                        ChoicePreference(
                            it.direction,
                            vm.getDirectionChoices(),
                            "Direction",
                            enabled = !it.defaultMode
                        )
                        SwitchPreference(
                            it.continuous,
                            "Continuous",
                            "If the reader is a pager or a scrolling window",
                            enabled = !it.defaultMode
                        )
                        val continuous by it.continuous.collectAsState()
                        ChoicePreference(
                            it.padding,
                            vm.getPaddingChoices(continuous),
                            if (continuous) "Page Padding" else "Border Padding"
                        )
                        if (!continuous) {
                            ChoicePreference(
                                it.imageScale,
                                vm.getImageScaleChoices(),
                                "Image Scale"
                            )
                        }
                        ChoicePreference(
                            it.navigationMode,
                            vm.getNavigationModeChoices(),
                            "Navigation mode"
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
