/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.categories

import cafe.adriel.voyager.navigator.Navigator

actual fun openCategoriesMenu(notifyFinished: () -> Unit, navigator: Navigator) {
    navigator push CategoriesScreen(notifyFinished)
}