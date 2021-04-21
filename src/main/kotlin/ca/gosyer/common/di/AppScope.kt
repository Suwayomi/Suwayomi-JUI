/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.common.di

import toothpick.Scope
import toothpick.ktp.KTP

/**
 * The global scope for dependency injection that will provide all the application level components.
 */
object AppScope : Scope by KTP.openRootScope() {

    /**
     * Returns a new subscope inheriting the root scope.
     */
    fun subscope(any: Any): Scope {
        return openSubScope(any)
    }

    /**
     * Returns an instance of [T] from the root scope.
     */
    inline fun <reified T> getInstance(): T {
        return getInstance(T::class.java)
    }

}