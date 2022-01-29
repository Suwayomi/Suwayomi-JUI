/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main.components

import ca.gosyer.data.update.UpdateChecker
import ca.gosyer.ui.base.vm.ViewModel
import me.tatarka.inject.annotations.Inject

class TrayViewModel @Inject constructor(
    private val updateChecker: UpdateChecker
) : ViewModel() {
    init {
        updateChecker.checkForUpdates()
    }
    val updateFound
        get() = updateChecker.updateFound
}
