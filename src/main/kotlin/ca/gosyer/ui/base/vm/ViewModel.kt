/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.vm

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class ViewModel {

    protected val scope = MainScope()

    fun destroy() {
        scope.cancel()
        onDestroy()
    }

    open fun onDestroy() {}
}
