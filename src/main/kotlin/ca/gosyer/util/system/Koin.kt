/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.system

import org.koin.core.context.GlobalContext

inline fun <reified T: Any> get() = GlobalContext.get().get<T>()

inline fun <reified T: Any> inject() = GlobalContext.get().inject<T>()