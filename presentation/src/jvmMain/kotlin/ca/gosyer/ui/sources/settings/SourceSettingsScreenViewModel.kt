/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.settings

import ca.gosyer.core.lang.throwIfCancellation
import ca.gosyer.data.models.sourcepreference.SourcePreference
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.ui.sources.settings.model.SourceSettingsView
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import me.tatarka.inject.annotations.Inject

class SourceSettingsScreenViewModel @Inject constructor(
    private val sourceHandler: SourceInteractionHandler,
    contextWrapper: ContextWrapper,
    private val params: Params
) : ViewModel(contextWrapper) {
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _sourceSettings = MutableStateFlow<List<SourceSettingsView<*, *>>>(emptyList())
    val sourceSettings = _sourceSettings.asStateFlow()

    init {
        getSourceSettings()
        sourceSettings.mapLatest { settings ->
            supervisorScope {
                settings.forEach { setting ->
                    setting.state.drop(1)
                        .filterNotNull()
                        .onEach {
                            sourceHandler.setSourceSetting(params.sourceId, setting.index, it)
                            getSourceSettings()
                        }
                        .launchIn(this)
                }
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
