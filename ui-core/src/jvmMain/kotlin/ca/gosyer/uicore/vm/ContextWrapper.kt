/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.uicore.vm

import dev.icerock.moko.resources.StringResource

expect class ContextWrapper {
    fun toPlatformString(stringResource: StringResource): String
    fun toPlatformString(stringResource: StringResource, vararg args: Any): String
}
