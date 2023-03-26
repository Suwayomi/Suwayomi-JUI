/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.lang

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat

// Taken from https://github.com/icerockdev/moko-resources/blob/master/resources/src/appleMain/kotlin/dev/icerock/moko/resources/desc/Utils.kt
actual fun stringFormat(string: String, vararg args: Any?): String {
    // NSString format works with NSObjects via %@, we should change standard format to %@
    val objcFormat = string.replace(Regex("%((?:\\.|\\d|\\$)*)[abcdefs]"), "%$1@")
    // bad but objc interop limited :(
    // When calling variadic C functions spread operator is supported only for *arrayOf(...)
    @Suppress("MagicNumber")
    return when (args.size) {
        0 -> NSString.stringWithFormat(objcFormat)
        1 -> NSString.stringWithFormat(objcFormat, args[0])
        2 -> NSString.stringWithFormat(objcFormat, args[0], args[1])
        3 -> NSString.stringWithFormat(objcFormat, args[0], args[1], args[2])
        4 -> NSString.stringWithFormat(objcFormat, args[0], args[1], args[2], args[3])
        5 -> NSString.stringWithFormat(objcFormat, args[0], args[1], args[2], args[3], args[4])
        6 -> NSString.stringWithFormat(
            objcFormat,
            args[0],
            args[1],
            args[2],
            args[3],
            args[4],
            args[5],
        )
        7 -> NSString.stringWithFormat(
            objcFormat,
            args[0],
            args[1],
            args[2],
            args[3],
            args[4],
            args[5],
            args[6],
        )
        8 -> NSString.stringWithFormat(
            objcFormat,
            args[0],
            args[1],
            args[2],
            args[3],
            args[4],
            args[5],
            args[6],
            args[7],
        )
        9 -> NSString.stringWithFormat(
            objcFormat,
            args[0],
            args[1],
            args[2],
            args[3],
            args[4],
            args[5],
            args[6],
            args[7],
            args[8],
        )
        else -> throw IllegalArgumentException("can't handle more then 9 arguments now")
    }
}
