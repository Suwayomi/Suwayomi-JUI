/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.vm

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.format
import me.tatarka.inject.annotations.Inject

actual class ContextWrapper @Inject constructor() {
    actual fun toPlatformString(stringResource: StringResource): String {
        return stringResource.localized()
    }
    actual fun toPlatformString(stringResource: StringResource, vararg args: Any): String {
        return stringResource.format(*args).localized()
    }
    actual fun toast(string: String, length: Length) {
    }
}
