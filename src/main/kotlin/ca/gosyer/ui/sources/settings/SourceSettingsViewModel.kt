/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.settings

import ca.gosyer.data.models.sourcepreference.SourcePreference
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.sources.settings.model.SourceSettingsView
import ca.gosyer.util.lang.throwIfCancellation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

class SourceSettingsViewModel @Inject constructor(
    private val params: Params,
    private val sourceHandler: SourceInteractionHandler
) : ViewModel() {
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _sourceSettings = MutableStateFlow<List<SourceSettingsView<*, *>>>(emptyList())
    val sourceSettings = _sourceSettings.asStateFlow()

    private val subscriptions: CopyOnWriteArrayList<Job> = CopyOnWriteArrayList()

    init {
        getSourceSettings()

        sourceSettings.onEach { settings ->
            subscriptions.forEach { it.cancel() }
            subscriptions.clear()
            subscriptions += settings.map { setting ->
                setting.state.drop(1).filterNotNull().onEach {
                    sourceHandler.setSourceSetting(params.sourceId, setting.index, it)
                    getSourceSettings()
                }.launchIn(scope)
            }
        }.launchIn(scope)
    }

    private fun getSourceSettings() {
        scope.launch {
            try {
                _sourceSettings.value = sourceHandler.getSourceSettings(params.sourceId).toView()
            } catch (e: Exception) {
                e.throwIfCancellation()
            } finally {
                _loading.value = false
            }
        }
    }

    data class Params(val sourceId: Long)

    private fun List<SourcePreference>.toView() = mapIndexed { index, sourcePreference ->
        SourceSettingsView(index, sourcePreference)
    }
}
