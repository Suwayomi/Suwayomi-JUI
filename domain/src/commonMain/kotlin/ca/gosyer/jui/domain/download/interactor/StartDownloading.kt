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
import com.diamondedge.logging.logging

@Inject
class StartDownloading(
    private val downloadRepository: DownloadRepository,
) {
    suspend fun await(onError: suspend (Throwable) -> Unit = {}) =
        asFlow()
            .catch {
                onError(it)
                log.warn(it) { "Failed to start downloader" }
            }
            .collect()

    fun asFlow() = downloadRepository.startDownloading()

    companion object {
        private val log = logging()
    }
}
