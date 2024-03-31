/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.interactor

import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreferenceChange
import ca.gosyer.jui.domain.source.service.SourceRepositoryOld
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class SetSourceSetting
    @Inject
    constructor(
        private val sourceRepositoryOld: SourceRepositoryOld,
    ) {
        suspend fun await(
            source: Source,
            settingIndex: Int,
            setting: Any,
            onError: suspend (Throwable) -> Unit = {
            },
        ) = asFlow(source, settingIndex, setting)
            .catch {
                onError(it)
                log.warn(it) { "Failed to set setting for ${source.displayName} with index = $settingIndex and value = $setting" }
            }
            .collect()

        suspend fun await(
            sourceId: Long,
            settingIndex: Int,
            setting: Any,
            onError: suspend (Throwable) -> Unit = {
            },
        ) = asFlow(sourceId, settingIndex, setting)
            .catch {
                onError(it)
                log.warn(it) { "Failed to set setting for $sourceId with index = $settingIndex and value = $setting" }
            }
            .collect()

        fun asFlow(
            source: Source,
            settingIndex: Int,
            setting: Any,
        ) = sourceRepositoryOld.setSourceSetting(
            source.id,
            SourcePreferenceChange(settingIndex, setting),
        )

        fun asFlow(
            sourceId: Long,
            settingIndex: Int,
            setting: Any,
        ) = sourceRepositoryOld.setSourceSetting(
            sourceId,
            SourcePreferenceChange(settingIndex, setting),
        )

        companion object {
            private val log = logging()
        }
    }
