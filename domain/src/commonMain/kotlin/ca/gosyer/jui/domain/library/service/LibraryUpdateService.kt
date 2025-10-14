/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.library.service

import ca.gosyer.jui.domain.base.WebsocketService
import ca.gosyer.jui.domain.library.model.UpdateStatus
import ca.gosyer.jui.domain.library.model.UpdaterJobsInfo
import com.diamondedge.logging.logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import me.tatarka.inject.annotations.Inject

@Inject
class LibraryUpdateService(
    private val libraryUpdateRepository: LibraryRepository,
) {
    fun getSubscription() = libraryUpdateRepository.libraryUpdateSubscription()
        .onStart {
            log.info { "Starting library update status subscription" }
            status.value = WebsocketService.Status.STARTING
        }
        .catch { error ->
            log.error(error) { "Error in library update status subscription" }
            status.value = WebsocketService.Status.STOPPED
        }
        .map { updates ->
            status.value = WebsocketService.Status.RUNNING
            if (updates.omittedUpdates) {
                log.info { "Omitted updates detected, fetching fresh library update status" }
                fetchLibraryUpdateStatus()
                return@map
            }
            if (updates.initial != null) {
                updateStatus.value = updates.initial
            }
            updates.jobsInfo.let { jobsInfo ->
                updateStatus.update {
                    it.copy(
                        jobsInfo = jobsInfo,
                        categoryUpdates = updates.categoryUpdates,
                        mangaUpdates = updates.mangaUpdates
                    )
                }
            }
        }

    private suspend fun fetchLibraryUpdateStatus() {
        val status = libraryUpdateRepository.libraryUpdateStatus().firstOrNull()
        if (status != null) {
            updateStatus.value = status
        }
    }

    companion object {
        private val log = logging()

        val status = MutableStateFlow(WebsocketService.Status.STARTING)
        val updateStatus = MutableStateFlow(
            UpdateStatus(
                emptyList(),
                emptyList(),
                UpdaterJobsInfo(
                    0,
                    false,
                    0,
                    0,
                    0,
                )
            )
        )
    }
}
