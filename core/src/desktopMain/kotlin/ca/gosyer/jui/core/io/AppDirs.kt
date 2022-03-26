/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.io

import ca.gosyer.jui.core.build.BuildKonfig
import net.harawata.appdirs.AppDirsFactory
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

val userDataDir: Path by lazy {
    AppDirsFactory.getInstance().getUserDataDir(BuildKonfig.NAME, null, null).toPath().also {
        if (!FileSystem.SYSTEM.exists(it)) {
            FileSystem.SYSTEM.createDirectories(it)
        }
    }
}
