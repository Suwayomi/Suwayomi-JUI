/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android

import android.annotation.SuppressLint
import android.content.Context
import ca.gosyer.jui.core.di.AppScope
import ca.gosyer.jui.data.DataComponent
import ca.gosyer.jui.data.create
import ca.gosyer.jui.ui.base.UiComponent
import ca.gosyer.jui.ui.base.create
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@AppScope
@Component
abstract class AppComponent(
    context: Context,
    val dataComponent: DataComponent = DataComponent.create(context),
    @Component
    val uiComponent: UiComponent = UiComponent.create(dataComponent)
) {

    abstract val appMigrations: AppMigrations

    @get:AppScope
    @get:Provides
    protected val appMigrationsFactory: AppMigrations
        get() = AppMigrations(dataComponent.migrationPreferences, uiComponent.contextWrapper)

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var appComponentInstance: AppComponent? = null

        fun getInstance(context: Context) = appComponentInstance ?: create(context)
            .also { appComponentInstance = it }
    }
}
