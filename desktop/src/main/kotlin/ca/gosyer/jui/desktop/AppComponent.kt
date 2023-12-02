/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.desktop

import ca.gosyer.jui.core.di.AppScope
import ca.gosyer.jui.data.DataComponent
import ca.gosyer.jui.domain.DomainComponent
import ca.gosyer.jui.ui.ViewModelComponent
import ca.gosyer.jui.ui.base.UiComponent
import ca.gosyer.jui.uicore.vm.ContextWrapper
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@AppScope
@Component
abstract class AppComponent(
    @get:Provides
    val context: ContextWrapper,
) : ViewModelComponent,
    DataComponent,
    DomainComponent,
    UiComponent {
    abstract val appMigrations: AppMigrations

    @get:AppScope
    @get:Provides
    protected val appMigrationsFactory: AppMigrations
        get() = AppMigrations(migrationPreferences, contextWrapper)

    val bind: ViewModelComponent
        @Provides get() = this

    companion object {
        private var appComponentInstance: AppComponent? = null

        fun getInstance(context: ContextWrapper) =
            appComponentInstance ?: create(context)
                .also { appComponentInstance = it }
    }
}
