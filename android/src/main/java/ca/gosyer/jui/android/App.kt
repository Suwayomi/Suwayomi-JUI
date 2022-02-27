/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import ca.gosyer.core.prefs.getAsFlow
import ca.gosyer.data.ui.model.ThemeMode
import ca.gosyer.ui.AppComponent
import kotlinx.coroutines.flow.launchIn

class App : Application(), DefaultLifecycleObserver {

    override fun onCreate() {
        super<Application>.onCreate()

        /*if (BuildConfig.DEBUG) {
            System.setProperty("kotlinx.coroutines.debug", "on")
        }*/

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        val appComponent = AppComponent.getInstance(this)
        appComponent.dataComponent.uiPreferences.themeMode()
            .getAsFlow {
                AppCompatDelegate.setDefaultNightMode(
                    when (it) {
                        ThemeMode.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        ThemeMode.Light -> AppCompatDelegate.MODE_NIGHT_NO
                        ThemeMode.Dark -> AppCompatDelegate.MODE_NIGHT_YES
                    }
                )
            }
            .launchIn(ProcessLifecycleOwner.get().lifecycleScope)

    }
}