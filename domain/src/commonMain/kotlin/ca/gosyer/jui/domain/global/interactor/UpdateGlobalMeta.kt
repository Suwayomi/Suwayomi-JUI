/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.global.interactor

import ca.gosyer.jui.domain.global.model.GlobalMeta
import ca.gosyer.jui.domain.global.service.GlobalRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class UpdateGlobalMeta @Inject constructor(private val globalRepository: GlobalRepository) {

    suspend fun await(
        globalMeta: GlobalMeta,
        example: Int = globalMeta.example,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(globalMeta, example)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update global meta" }
        }
        .collect()

    fun asFlow(
        globalMeta: GlobalMeta,
        example: Int = globalMeta.example,
    ) = flow {
        if (example != globalMeta.example) {
            globalRepository.updateGlobalMeta(
                "example",
                example.toString(),
            ).collect()
        }
        emit(Unit)
    }

    companion object {
        private val log = logging()
    }
}
