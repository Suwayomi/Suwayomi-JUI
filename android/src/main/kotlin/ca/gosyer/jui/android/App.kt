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
import ca.gosyer.jui.android.data.notification.Notifications
import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.getAsFlow
import ca.gosyer.jui.domain.ui.model.ThemeMode
import kotlinx.coroutines.flow.launchIn
import org.lighthousegames.logging.logging
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

        appComponent.uiPreferences.themeMode()
            .getAsFlow {
                AppCompatDelegate.setDefaultNightMode(
                    when (it) {
                        ThemeMode.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        ThemeMode.Light -> AppCompatDelegate.MODE_NIGHT_NO
                        ThemeMode.Dark -> AppCompatDelegate.MODE_NIGHT_YES
                    },
                )
            }
            .launchIn(ProcessLifecycleOwner.get().lifecycleScope)

        setupAppLanguage(appComponent.uiPreferences.language())
    }

    private fun setupNotificationChannels() {
        try {
            Notifications.createChannels(this)
        } catch (e: Exception) {
            log.error(e) { "Failed to modify notification channels" }
        }
    }

    private fun setupAppLanguage(languagePref: Preference<String>) {
        languagePref
            .getAsFlow {
                if (languagePref.isSet() && languagePref.defaultValue() != it) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.create(
                            Locale.forLanguageTag(it),
                            Locale.forLanguageTag("en"),
                        ),
                    )
                } else if (
                    AppCompatDelegate.getApplicationLocales().isEmpty.not() &&
                    languagePref.isSet() &&
                    it == languagePref.defaultValue()
                ) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                }
            }
            .launchIn(ProcessLifecycleOwner.get().lifecycleScope)
    }

    private companion object {
        private val log = logging()
    }
}
