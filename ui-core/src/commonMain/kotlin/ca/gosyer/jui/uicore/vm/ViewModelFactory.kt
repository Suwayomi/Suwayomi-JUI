/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.vm

import androidx.compose.runtime.compositionLocalOf
import kotlin.reflect.KClass

abstract class ViewModelFactory {
    inline fun <reified VM : ViewModel> instantiate(arg1: Any? = null): VM {
        return instantiate(VM::class, arg1)
    }

    abstract fun <VM : ViewModel> instantiate(klass: KClass<VM>, arg1: Any? = null): VM
}

val LocalViewModelFactory =
    compositionLocalOf<ViewModelFactory> { throw IllegalArgumentException("ViewModelFactory not found") }
