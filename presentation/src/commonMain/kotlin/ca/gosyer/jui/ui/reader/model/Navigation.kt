/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.model

sealed class Navigation(
    val name: String,
) {
    data object MENU : Navigation("Menu")

    data object PREV : Navigation("Prev")

    data object NEXT : Navigation("Next")

    data object LEFT : Navigation("Left")

    data object RIGHT : Navigation("Right")

    data object UP : Navigation("Up")

    data object DOWN : Navigation("Down")
}
