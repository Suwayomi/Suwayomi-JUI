/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui

import ca.gosyer.data.DataComponent
import ca.gosyer.ui.base.UiComponent
import ca.gosyer.uicore.vm.ContextWrapper

expect abstract class AppComponent {
    val dataComponent: DataComponent
    val uiComponent: UiComponent

    abstract val contextWrapper: ContextWrapper
}
