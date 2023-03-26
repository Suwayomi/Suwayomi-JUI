/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.reader

import ca.gosyer.jui.core.prefs.getAsFlow
import ca.gosyer.jui.domain.reader.service.ReaderModePreferences
import ca.gosyer.jui.domain.reader.service.ReaderPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest

class ReaderModeWatch(
    private val readerPreferences: ReaderPreferences,
    private val scope: CoroutineScope,
    private val mode: StateFlow<String> = readerPreferences.mode().stateIn(scope),
    initialPreferences: ReaderModePreferences = readerPreferences.getMode(
        mode.value,
    ),
) {
    private val preferenceJobs = mutableListOf<Job>()
    val direction = MutableStateFlow(initialPreferences.direction().get())
    val continuous = MutableStateFlow(initialPreferences.continuous().get())
    val padding = MutableStateFlow(initialPreferences.padding().get())
    val imageScale = MutableStateFlow(initialPreferences.imageScale().get())
    val fitSize = MutableStateFlow(initialPreferences.fitSize().get())
    val maxSize = MutableStateFlow(initialPreferences.maxSize().get())
    val navigationMode = MutableStateFlow(initialPreferences.navigationMode().get())

    init {
        setupJobs(mode.value)
        mode
            .mapLatest { mode ->
                setupJobs(mode)
            }
            .launchIn(scope)
    }

    private fun setupJobs(mode: String) {
        preferenceJobs.forEach {
            it.cancel()
        }
        preferenceJobs.clear()
        val preferences = readerPreferences.getMode(mode)
        preferenceJobs += preferences.direction()
            .getAsFlow {
                direction.value = it
            }
            .launchIn(scope)

        preferenceJobs += preferences.continuous()
            .getAsFlow {
                continuous.value = it
            }
            .launchIn(scope)

        preferenceJobs += preferences.padding()
            .getAsFlow {
                padding.value = it
            }
            .launchIn(scope)

        preferenceJobs += preferences.imageScale()
            .getAsFlow {
                imageScale.value = it
            }
            .launchIn(scope)

        preferenceJobs += preferences.fitSize()
            .getAsFlow {
                fitSize.value = it
            }
            .launchIn(scope)

        preferenceJobs += preferences.maxSize()
            .getAsFlow {
                maxSize.value = it
            }
            .launchIn(scope)

        preferenceJobs += preferences.navigationMode()
            .getAsFlow {
                navigationMode.value = it
            }
            .launchIn(scope)
    }
}
