/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base

import ca.gosyer.jui.core.di.AppScope
import ca.gosyer.jui.data.DataComponent
import ca.gosyer.jui.ui.base.image.KamelConfigProvider
import ca.gosyer.jui.ui.base.vm.ViewModelFactoryImpl
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.LocalViewModelFactory
import io.kamel.core.config.KamelConfig
import io.kamel.image.config.LocalKamelConfig
import io.kamel.image.config.resourcesFetcher
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@AppScope
@Component
actual abstract class UiComponent(
    @Component actual val dataComponent: DataComponent
) {
    protected actual abstract val kamelConfigProvider: KamelConfigProvider

    actual abstract val viewModelFactory: ViewModelFactoryImpl

    actual abstract val kamelConfig: KamelConfig

    actual abstract val contextWrapper: ContextWrapper

    @get:AppScope
    @get:Provides
    protected actual val kamelConfigFactory: KamelConfig
        get() = kamelConfigProvider.get { resourcesFetcher(dataComponent.context) }

    actual fun getHooks() = arrayOf(
        LocalViewModelFactory provides viewModelFactory,
        LocalKamelConfig provides kamelConfig
    )

    actual companion object
}
