/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about.components

import ca.gosyer.jui.presentation.build.BuildKonfig
import platform.UIKit.UIDevice

actual fun getDebugInfo(): String {
    val device = UIDevice.currentDevice
    return """
        App version: ${BuildKonfig.VERSION} (${ if (BuildKonfig.DEBUG) "Debug" else "Standard"}, ${BuildKonfig.MIGRATION_CODE})
        Preview build: r${BuildKonfig.PREVIEW_BUILD}
        Device name: ${device.name}
        Device model: ${device.model}
        System name: ${device.systemName}
        System version: ${device.systemVersion}
    """.trimIndent()
}
