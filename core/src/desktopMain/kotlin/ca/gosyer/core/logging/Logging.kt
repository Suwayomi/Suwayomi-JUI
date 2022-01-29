/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.core.logging

import mu.KLogger
import mu.KotlinLogging

abstract class CKLogger(logger: KLogger) : KLogger by logger {
    constructor(func: () -> Unit) : this(kLogger(func))
}

fun kLogger(func: () -> Unit) = KotlinLogging.logger(func)
