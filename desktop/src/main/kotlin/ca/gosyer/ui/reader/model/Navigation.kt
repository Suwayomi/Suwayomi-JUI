/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader.model

sealed class Navigation(val name: String) {
    object NONE : Navigation("None")
    object PREV : Navigation("Prev")
    object NEXT : Navigation("Next")
    object LEFT : Navigation("Left")
    object RIGHT : Navigation("Right")
}
