/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.settings

import ca.gosyer.jui.domain.source.interactor.GetSourceSettings
import ca.gosyer.jui.domain.source.interactor.SetSourceSetting
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreference
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

@Inject
class SourceSettingsScreenViewModel(
    private val getSourceSettings: GetSourceSettings,
    private val setSourceSetting: SetSourceSetting,
    contextWrapper: ContextWrapper,
    @Assisted private val params: Params,
) : ViewModel(contextWrapper) {
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _sourceSettings = MutableStateFlow<ImmutableList<SourceSettingsView<*, *>>>(persistentListOf())
    val sourceSettings = _sourceSettings.asStateFlow()

    init {
        getSourceSettings()
        sourceSettings.mapLatest { settings ->
            supervisorScope {
                settings.forEach { setting ->
                    setting.state.drop(1)
                        .filterNotNull()
                        .onEach {
                            setSourceSetting.await(
                                sourceId = params.sourceId,
                                setting.props,
                                onError = { toast(it.message.orEmpty()) },
                            )
                            getSourceSettings()
                        }
                        .launchIn(this)
                }
            }
        }.launchIn(scope)
    }

    private fun getSourceSettings() {
        getSourceSettings.asFlow(params.sourceId)
            .onEach {
                _sourceSettings.value = it.toView()
                _loading.value = false
            }
            .catch {
                toast(it.message.orEmpty())
                log.warn(it) { "Error setting source setting" }
                _loading.value = false
            }
            .launchIn(scope)
    }

    data class Params(
        val sourceId: Long,
    )

    private fun List<SourcePreference>.toView() =
        mapIndexed { index, sourcePreference ->
            SourceSettingsView(index, sourcePreference)
        }.toImmutableList()

    private companion object {
        private val log = logging()
    }
}
