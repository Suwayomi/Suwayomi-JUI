/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.settings

import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreferenceChange
import ca.gosyer.jui.domain.source.service.SourceRepository
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class SourceSettingsScreenViewModel @Inject constructor(
    private val sourceHandler: SourceRepository,
    contextWrapper: ContextWrapper,
    private val params: Params
) : ViewModel(contextWrapper) {
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _sourceSettings = MutableStateFlow<ImmutableList<StableHolder<SourceSettingsView<*, *>>>>(persistentListOf())
    val sourceSettings = _sourceSettings.asStateFlow()

    init {
        getSourceSettings()
        sourceSettings.mapLatest { settings ->
            supervisorScope {
                settings.forEach { (setting) ->
                    setting.state.drop(1)
                        .filterNotNull()
                        .onEach {
                            sourceHandler.setSourceSetting(params.sourceId, SourcePreferenceChange(setting.index, it))
                                .catch {
                                    log.warn(it) { "Error setting source setting" }
                                }
                                .collect()
                            getSourceSettings()
                        }
                        .launchIn(this)
                }
            }
        }.launchIn(scope)
    }

    private fun getSourceSettings() {
        sourceHandler.getSourceSettings(params.sourceId)
            .onEach {
                _sourceSettings.value = it.toView()
                _loading.value = false
            }
            .catch {
                log.warn(it) { "Error setting source setting" }
                _loading.value = false
            }
            .launchIn(scope)
    }

    data class Params(val sourceId: Long)

    private fun List<SourcePreference>.toView() = mapIndexed { index, sourcePreference ->
        SourceSettingsView(index, sourcePreference)
    }.map(::StableHolder).toImmutableList()

    private companion object {
        private val log = logging()
    }
}
