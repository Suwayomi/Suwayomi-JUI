/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.ui.model.Screen
import ca.gosyer.ui.base.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    uiPreferences: UiPreferences
): ViewModel() {
    private val _menu = MutableStateFlow(
            uiPreferences.startScreen().get().toMenu()
    )
    val menu = _menu.asStateFlow()

    fun switchMenu(menu: Menu) {
        scope.launch {
            _menu.value = menu
        }
    }

    fun Screen.toMenu() = when (this) {
        Screen.Library -> Menu.Library
        Screen.Sources -> Menu.Sources
        Screen.Extensions -> Menu.Extensions
    }
}

enum class Menu {
    Library,
    Sources,
    Extensions,
    Manga,
    Categories
}