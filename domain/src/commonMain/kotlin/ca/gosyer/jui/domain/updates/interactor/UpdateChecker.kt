/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.updates.interactor

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.domain.build.BuildKonfig
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.updates.model.GithubRelease
import ca.gosyer.jui.domain.updates.service.UpdatePreferences
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class UpdateChecker @Inject constructor(
    private val updatePreferences: UpdatePreferences,
    private val client: Http
) {
    suspend fun await(manualFetch: Boolean) = asFlow(manualFetch)
        .catch { log.warn(it) { "Failed to check for updates" } }
        .singleOrNull()

    fun asFlow(manualFetch: Boolean) = flow {
        if (!manualFetch && !updatePreferences.enabled().get()) return@flow
        val latestRelease = client.get(
            "https://api.github.com/repos/$GITHUB_REPO/releases/latest"
        ).body<GithubRelease>()

        if (isNewVersion(latestRelease.version)) {
            emit(Update.UpdateFound(latestRelease))
        } else {
            emit(Update.NoUpdatesFound)
        }
    }.flowOn(Dispatchers.IO)

    sealed class Update {
        data class UpdateFound(val release: GithubRelease) : Update()
        object NoUpdatesFound : Update()
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

        private val RELEASE_TAG: String by lazy {
            if (BuildKonfig.IS_PREVIEW) {
                "r${BuildKonfig.PREVIEW_BUILD}"
            } else {
                "v${BuildKonfig.VERSION}"
            }
        }

        val RELEASE_URL = "https://github.com/$GITHUB_REPO/releases/tag/$RELEASE_TAG"

        private val log = logging()
    }
}
