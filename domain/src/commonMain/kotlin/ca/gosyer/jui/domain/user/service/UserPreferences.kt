package ca.gosyer.jui.domain.user.service

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore

class UserPreferences(
    private val preferenceStore: PreferenceStore,
) {
    fun uiRefreshToken(): Preference<String> = preferenceStore.getString("ui_refresh_token", "")

    fun uiAccessToken(): Preference<String> = preferenceStore.getString("ui_refresh_token", "")

    fun simpleSession(): Preference<String> = preferenceStore.getString("simple_session", "")
}
