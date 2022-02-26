/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.vm

import ca.gosyer.ui.base.theme.AppThemeViewModel
import ca.gosyer.ui.updates.UpdatesScreenViewModel
import ca.gosyer.uicore.vm.ViewModel
import ca.gosyer.uicore.vm.ViewModelFactory
import me.tatarka.inject.annotations.Inject
import kotlin.reflect.KClass

@Inject
actual class ViewModelFactoryImpl(
    private val appThemeFactory: () -> AppThemeViewModel,
    private val updatesFactory: () -> UpdatesScreenViewModel
) : ViewModelFactory() {

    override fun <VM : ViewModel> instantiate(klass: KClass<VM>, arg1: Any?): VM {
        @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
        return when (klass) {
            AppThemeViewModel::class -> appThemeFactory()
            UpdatesScreenViewModel::class -> updatesFactory()
            else -> throw IllegalArgumentException("Unknown ViewModel $klass")
        } as VM
    }
}
