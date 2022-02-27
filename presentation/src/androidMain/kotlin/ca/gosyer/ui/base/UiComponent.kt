/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base

import ca.gosyer.core.di.AppScope
import ca.gosyer.data.DataComponent
import ca.gosyer.ui.base.image.KamelConfigProvider
import ca.gosyer.ui.base.vm.ViewModelFactoryImpl
import ca.gosyer.uicore.vm.LocalViewModelFactory
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

    @get:AppScope
    @get:Provides
    actual protected val kamelConfigFactory: KamelConfig
        get() = kamelConfigProvider.get { resourcesFetcher(dataComponent.context) }

    actual fun getHooks() = arrayOf(
        LocalViewModelFactory provides viewModelFactory,
        LocalKamelConfig provides kamelConfig
    )

    actual companion object
}
