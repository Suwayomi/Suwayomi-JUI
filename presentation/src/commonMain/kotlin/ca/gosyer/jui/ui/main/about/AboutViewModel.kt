/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about

import ca.gosyer.jui.data.models.About
import ca.gosyer.jui.data.server.interactions.SettingsInteractionHandler
import ca.gosyer.jui.data.update.UpdateChecker
import ca.gosyer.jui.data.update.UpdateChecker.Update
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import com.soywiz.klock.KlockLocale
import com.soywiz.klock.format
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AboutViewModel @Inject constructor(
    private val settingsHandler: SettingsInteractionHandler,
    private val updateChecker: UpdateChecker,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {

    private val _about = MutableStateFlow<About?>(null)
    val about = _about.asStateFlow()

    val formattedBuildTime = about.map { about ->
        about ?: return@map ""
        getFormattedDate(about.buildTime.seconds)
    }.stateIn(scope, SharingStarted.Eagerly, "")

    private val _updates = MutableSharedFlow<Update.UpdateFound>()
    val updates = _updates.asSharedFlow()

    init {
        getAbout()
    }

    private fun getAbout() {
        settingsHandler.aboutServer()
            .onEach {
                _about.value = it
            }
            .launchIn(scope)
    }

    fun checkForUpdates() {
        updateChecker.checkForUpdates()
            .filterIsInstance<Update.UpdateFound>()
            .onEach {
                _updates.emit(it)
            }
            .launchIn(scope)
    }

    private fun getFormattedDate(time: Duration): String {
        return KlockLocale.default.formatDateTimeMedium.format(time.inWholeMilliseconds)
    }
}
