/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import ca.gosyer.core.logging.CKLogger
import ca.gosyer.core.prefs.Preference
import ca.gosyer.core.prefs.getAsFlow
import ca.gosyer.data.ui.model.ThemeMode
import ca.gosyer.jui.android.data.notification.Notifications
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale

class App : Application(), DefaultLifecycleObserver {

    override fun onCreate() {
        super<Application>.onCreate()

        if (BuildConfig.DEBUG) {
            System.setProperty("kotlinx.coroutines.debug", "on")
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        val appComponent = AppComponent.getInstance(this)

        setupNotificationChannels()

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

        setupAppLanguage(appComponent.dataComponent.uiPreferences.language())
    }

    private fun setupNotificationChannels() {
        try {
            Notifications.createChannels(this)
        } catch (e: Exception) {
            error(e) { "Failed to modify notification channels" }
        }
    }

    private fun setupAppLanguage(languagePref: Preference<String>) {
        val defaultLocaleList = AppCompatDelegate.getApplicationLocales()
        if (languagePref.isSet() && languagePref.defaultValue() != languagePref.get()) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.create(
                    Locale.forLanguageTag(languagePref.get()),
                    Locale.forLanguageTag("en")
                )
            )
        }
        languagePref
            .changes()
            .onEach {
                if (languagePref.isSet() && languagePref.defaultValue() != it) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.create(
                            Locale.forLanguageTag(it),
                            Locale.forLanguageTag("en")
                        )
                    )
                } else if (languagePref.isSet() && it == languagePref.defaultValue()) {
                    AppCompatDelegate.setApplicationLocales(defaultLocaleList)
                }
            }
            .launchIn(ProcessLifecycleOwner.get().lifecycleScope)
    }

    protected companion object : CKLogger({})
}
