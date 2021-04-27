/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.system

import java.io.File

fun processFile() {
    val pidFile = ProcessHandle.current().pid()
    val strTmp = System.getProperty("java.io.tmpdir")
    val file = File("$strTmp/TachideskJUI.pid")

    // backup deletion
    if (file.exists()) {
        file.delete()
    }

    file.writeText(pidFile.toString())

    file.deleteOnExit()
}
