/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.extension.interactor

import ca.gosyer.jui.domain.extension.service.ExtensionRepository
import com.diamondedge.logging.logging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import okio.FileSystem
import okio.Path
import okio.SYSTEM

@Inject
class InstallExtensionFile(
    private val extensionRepository: ExtensionRepository,
) {
    suspend fun await(
        path: Path,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(path)
        .catch {
            onError(it)
            log.warn(it) { "Failed to install extension from $path" }
        }
        .collect()

    fun asFlow(path: Path) = extensionRepository.installExtension(FileSystem.SYSTEM.source(path))

    companion object {
        private val log = logging()
    }
}
