/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.download.interactor

import ca.gosyer.jui.domain.download.service.DownloadRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class StopDownloading @Inject constructor(private val downloadRepository: DownloadRepository) {

    suspend fun await() = asFlow()
        .catch { log.warn(it) { "Failed to stop downloader" } }
        .collect()

    fun asFlow() = downloadRepository.stopDownloading()

    companion object {
        private val log = logging()
    }
}
