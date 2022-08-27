/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.lang

fun Long.bytesIntoHumanReadable(si: Boolean = true): String {
    val bytes = this
    val unit = if (si) 1000L else 1024
    val i = if (si) "" else "i"
    val kilobyte: Long = unit
    val megabyte = kilobyte * unit
    val gigabyte = megabyte * unit
    val terabyte = gigabyte * unit
    return if (bytes in 0 until kilobyte) {
        "$bytes B"
    } else if (bytes in kilobyte until megabyte) {
        "${(bytes / kilobyte)} K${i}B"
    } else if (bytes in megabyte until gigabyte) {
        "${(bytes / megabyte)} M${i}B"
    } else if (bytes in gigabyte until terabyte) {
        "${(bytes / gigabyte)} G${i}B"
    } else if (bytes >= terabyte) {
        "${(bytes / terabyte)} T${i}B"
    } else {
        "$bytes Bytes"
    }
}