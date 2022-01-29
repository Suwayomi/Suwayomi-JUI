/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.core.io

import ca.gosyer.core.build.BuildKonfig
import mu.KotlinLogging
import net.harawata.appdirs.AppDirsFactory
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

private val logger = KotlinLogging.logger {}

val userDataDir: Path by lazy {
    AppDirsFactory.getInstance().getUserDataDir(BuildKonfig.NAME, null, null).toPath().also {
        if (!FileSystem.SYSTEM.exists(it)) {
            logger.info("Attempted to create app data dir, result: {}", FileSystem.SYSTEM.createDirectories(it))
        }
    }
}
