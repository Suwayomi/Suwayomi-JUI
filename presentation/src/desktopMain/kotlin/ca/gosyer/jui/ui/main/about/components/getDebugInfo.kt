/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about.components

import ca.gosyer.jui.presentation.build.BuildKonfig
import java.lang.management.ManagementFactory

actual fun getDebugInfo(): String {
    val runtime = ManagementFactory.getRuntimeMXBean()
    val os = ManagementFactory.getOperatingSystemMXBean()
    return """
        App version: ${BuildKonfig.VERSION} (${ if (BuildKonfig.DEBUG) "Debug" else "Standard"}, ${BuildKonfig.MIGRATION_CODE})
        Preview build: r${BuildKonfig.PREVIEW_BUILD}
        Device system: ${os.name} (${os.version})
        Device arch: ${os.arch}
        Device processors: ${os.availableProcessors}
        Runtime name: ${runtime.vmName}
        Runtime vendor: ${runtime.vmVendor}
        Runtime version: ${runtime.vmVersion}
    """.trimIndent()
}