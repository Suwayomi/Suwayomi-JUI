/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base

import ca.gosyer.jui.core.di.AppScope
import ca.gosyer.jui.ui.base.image.KamelConfigProvider
import ca.gosyer.jui.ui.base.vm.ViewModelFactoryImpl
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.LocalViewModelFactory
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.KamelConfigBuilder
import io.kamel.image.config.LocalKamelConfig
import me.tatarka.inject.annotations.Provides

interface UiComponent {
    val kamelConfigProvider: KamelConfigProvider

    val viewModelFactory: ViewModelFactoryImpl

    val kamelConfig: KamelConfig

    val contextWrapper: ContextWrapper

    @AppScope
    @Provides
    fun kamelConfigFactory(contextWrapper: ContextWrapper): KamelConfig = kamelConfigProvider.get { kamelPlatformHandler(contextWrapper) }

    fun getHooks() = arrayOf(
        LocalViewModelFactory provides viewModelFactory,
        LocalKamelConfig provides kamelConfig
    )
}

expect fun KamelConfigBuilder.kamelPlatformHandler(contextWrapper: ContextWrapper)
