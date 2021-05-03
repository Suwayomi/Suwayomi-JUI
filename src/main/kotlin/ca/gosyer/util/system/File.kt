/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.system

import ca.gosyer.BuildConfig
import net.harawata.appdirs.AppDirs
import net.harawata.appdirs.AppDirsFactory
import java.io.File

val appDirs: AppDirs by lazy {
    AppDirsFactory.getInstance()
}

val userDataDir: File by lazy {
    File(appDirs.getUserDataDir(BuildConfig.NAME, null, null)).also {
        if (!it.exists()) it.mkdirs()
    }
}
