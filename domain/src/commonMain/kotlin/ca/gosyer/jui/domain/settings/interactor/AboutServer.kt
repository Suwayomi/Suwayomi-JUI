/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.settings.interactor

import ca.gosyer.jui.domain.settings.service.SettingsRepository
import ca.gosyer.jui.domain.updates.service.UpdatesRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class AboutServer @Inject constructor(private val settingsRepository: SettingsRepository) {

    suspend fun await() = asFlow()
        .catch { log.warn(it) { "Failed to get server information" } }
        .singleOrNull()

    fun asFlow() = settingsRepository.aboutServer()

    companion object {
        private val log = logging()
    }
}
