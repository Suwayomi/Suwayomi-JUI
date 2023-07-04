/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.extension.interactor

import ca.gosyer.jui.domain.extension.model.Extension
import ca.gosyer.jui.domain.extension.service.ExtensionRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class InstallExtension
    @Inject
    constructor(private val extensionRepository: ExtensionRepository) {
        suspend fun await(
            extension: Extension,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(extension)
            .catch {
                onError(it)
                log.warn(it) { "Failed to install extension ${extension.apkName}" }
            }
            .collect()

        fun asFlow(extension: Extension) = extensionRepository.installExtension(extension.pkgName)

        companion object {
            private val log = logging()
        }
    }
