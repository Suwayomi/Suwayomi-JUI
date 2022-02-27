/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui

import ca.gosyer.core.di.AppScope
import ca.gosyer.data.DataComponent
import ca.gosyer.data.create
import ca.gosyer.ui.base.UiComponent
import ca.gosyer.ui.base.create
import ca.gosyer.uicore.vm.ContextWrapper
import me.tatarka.inject.annotations.Component

@AppScope
@Component
actual abstract class AppComponent constructor(
    actual val dataComponent: DataComponent = DataComponent.create(),
    @Component
    actual val uiComponent: UiComponent = UiComponent.create(dataComponent)
) {
    actual abstract val contextWrapper: ContextWrapper
    companion object {
        private var appComponentInstance: AppComponent? = null

        fun getInstance() = appComponentInstance ?: create()
            .also { appComponentInstance = it }
    }
}
