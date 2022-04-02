/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about.components

import android.os.Build
import ca.gosyer.jui.presentation.build.BuildKonfig

actual fun getDebugInfo(): String {
    return """
        App version: ${BuildKonfig.VERSION} (${ if (BuildKonfig.DEBUG) "Debug" else "Standard"}, ${BuildKonfig.MIGRATION_CODE})
        Preview build: r${BuildKonfig.PREVIEW_BUILD}
        Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
        Android build ID: ${Build.DISPLAY}
        Device brand: ${Build.BRAND}
        Device manufacturer: ${Build.MANUFACTURER}
        Device name: ${Build.DEVICE}
        Device model: ${Build.MODEL}
        Device product name: ${Build.PRODUCT}
    """.trimIndent()
}
