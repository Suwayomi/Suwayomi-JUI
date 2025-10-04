/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.settings.service

import ca.gosyer.jui.domain.settings.model.About
import ca.gosyer.jui.domain.settings.model.SetSettingsInput
import ca.gosyer.jui.domain.settings.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<Settings>

    fun setSettings(input: SetSettingsInput): Flow<Unit>

    fun aboutServer(): Flow<About>
}
