/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.vm

import ca.gosyer.jui.core.lang.launchDefault
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.tatarka.inject.annotations.Inject

actual class ContextWrapper
    @Inject
    constructor() {
        private val _toasts = MutableSharedFlow<Pair<String, Length>>()
        val toasts = _toasts.asSharedFlow()

        actual fun toPlatformString(stringResource: StringResource): String {
            return stringResource.desc().localized()
        }
        actual fun toPlatformString(
            stringResource: StringResource,
            vararg args: Any,
        ): String {
            return stringResource.format(*args).localized()
        }
        actual fun toast(
            string: String,
            length: Length,
        ) {
            GlobalScope.launchDefault {
                _toasts.emit(string to length)
            }
        }
    }
