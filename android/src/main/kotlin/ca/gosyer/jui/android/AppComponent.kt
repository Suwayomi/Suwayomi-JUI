/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android

import android.annotation.SuppressLint
import android.content.Context
import ca.gosyer.jui.core.di.AppScope
import ca.gosyer.jui.domain.DomainComponent
import ca.gosyer.jui.ui.base.UiComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@AppScope
@Component
abstract class AppComponent(
    @get:AppScope
    @get:Provides
    val context: Context
) : DomainComponent, UiComponent {

    abstract val appMigrations: AppMigrations

    @get:AppScope
    @get:Provides
    protected val appMigrationsFactory: AppMigrations
        get() = AppMigrations(migrationPreferences, contextWrapper)

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var appComponentInstance: AppComponent? = null

        fun getInstance(context: Context) = appComponentInstance ?: create(context)
            .also { appComponentInstance = it }
    }
}
