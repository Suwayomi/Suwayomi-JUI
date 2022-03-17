/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base

import androidx.compose.runtime.ProvidedValue
import ca.gosyer.data.DataComponent
import ca.gosyer.ui.base.image.KamelConfigProvider
import ca.gosyer.ui.base.vm.ViewModelFactoryImpl
import ca.gosyer.uicore.vm.ContextWrapper
import io.kamel.core.config.KamelConfig

expect abstract class UiComponent {
    protected val dataComponent: DataComponent
    protected abstract val kamelConfigProvider: KamelConfigProvider

    abstract val viewModelFactory: ViewModelFactoryImpl

    abstract val kamelConfig: KamelConfig

    abstract val contextWrapper: ContextWrapper

    protected val kamelConfigFactory: KamelConfig

    fun getHooks(): Array<ProvidedValue<out Any>>

    companion object
}
