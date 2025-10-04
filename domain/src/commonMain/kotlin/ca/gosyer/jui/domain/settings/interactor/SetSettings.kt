/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.settings.interactor

import ca.gosyer.jui.domain.settings.model.SetSettingsInput
import ca.gosyer.jui.domain.settings.service.SettingsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

@Inject
class SetSettings(
    private val settingsRepository: SettingsRepository,
) {
    suspend fun await(
        input: SetSettingsInput,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(input)
        .catch {
            onError(it)
            log.warn(it) { "Failed to check for server updates" }
        }
        .singleOrNull()

    fun asFlow(input: SetSettingsInput) = settingsRepository.setSettings(input)

    companion object {
        private val log = logging()
    }
}
