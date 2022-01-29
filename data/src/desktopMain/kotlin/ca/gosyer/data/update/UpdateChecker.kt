/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.update

import ca.gosyer.core.lang.launch
import ca.gosyer.core.lang.withIOContext
import ca.gosyer.data.build.BuildKonfig
import ca.gosyer.data.server.Http
import ca.gosyer.data.update.model.GithubRelease
import io.ktor.client.request.get
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.tatarka.inject.annotations.Inject

class UpdateChecker @Inject constructor(
    private val updatePreferences: UpdatePreferences,
    private val client: Http
) {
    private val _updateFound = MutableSharedFlow<GithubRelease>()
    val updateFound = _updateFound.asSharedFlow()

    @OptIn(DelicateCoroutinesApi::class)
    fun checkForUpdates() {
        if (!updatePreferences.enabled().get()) return
        launch {
            val latestRelease = withIOContext {
                client.get<GithubRelease>("https://api.github.com/repos/$GITHUB_REPO/releases/latest")
            }
            if (isNewVersion(latestRelease.version)) {
                _updateFound.emit(latestRelease)
            }
        }
    }

    // Thanks to Tachiyomi for inspiration
    private fun isNewVersion(versionTag: String): Boolean {
        // Removes prefixes like "r" or "v"
        val newVersion = versionTag.replace("[^\\d.]".toRegex(), "")

        return if (BuildKonfig.IS_PREVIEW) {
            // Preview builds: based on releases in "Suwayomi/Tachidesk-JUI-preview" repo
            // tagged as something like "r123"
            newVersion.toInt() > BuildKonfig.PREVIEW_BUILD
        } else {
            // Release builds: based on releases in "Suwayomi/Tachidesk-JUI" repo
            // tagged as something like "v1.1.2"
            newVersion != BuildKonfig.VERSION
        }
    }

    companion object {
        private val GITHUB_REPO = if (BuildKonfig.IS_PREVIEW) {
            "Suwayomi/Tachidesk-JUI-preview"
        } else {
            "Suwayomi/Tachidesk-JUI"
        }
    }
}
