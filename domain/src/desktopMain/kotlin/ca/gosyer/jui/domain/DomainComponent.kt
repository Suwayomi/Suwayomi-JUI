/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain

import ca.gosyer.jui.core.di.AppScope
import ca.gosyer.jui.domain.server.service.ServerHostPreferences
import ca.gosyer.jui.domain.server.service.ServerService
import me.tatarka.inject.annotations.Provides

actual interface DomainComponent : SharedDomainComponent {
    // Singletons

    val serverService: ServerService

    val serverHostPreferences: ServerHostPreferences

    @get:AppScope
    @get:Provides
    val serverServiceFactory: ServerService
        get() = ServerService(serverHostPreferences)

    companion object
}
