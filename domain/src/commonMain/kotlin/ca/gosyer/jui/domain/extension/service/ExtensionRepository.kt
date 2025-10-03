/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.extension.service

import ca.gosyer.jui.domain.extension.model.Extension
import kotlinx.coroutines.flow.Flow
import okio.Source

interface ExtensionRepository {

    fun getExtensionList(): Flow<List<Extension>>

    fun installExtension(
        source: Source,
    ): Flow<Unit>

    fun installExtension(
        pkgName: String,
    ): Flow<Unit>

    fun updateExtension(
        pkgName: String,
    ): Flow<Unit>

    fun uninstallExtension(
        pkgName: String,
    ): Flow<Unit>
}
