/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.extension.interactor

import ca.gosyer.jui.domain.extension.service.ExtensionRepository
import com.diamondedge.logging.logging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject

@Inject
class GetExtensionList(
    private val extensionRepository: ExtensionRepository,
) {
    suspend fun await(onError: suspend (Throwable) -> Unit = {}) =
        asFlow()
            .catch {
                onError(it)
                log.warn(it) { "Failed to get extension list" }
            }
            .singleOrNull()

    fun asFlow() = extensionRepository.getExtensionList()

    companion object {
        private val log = logging()
    }
}
