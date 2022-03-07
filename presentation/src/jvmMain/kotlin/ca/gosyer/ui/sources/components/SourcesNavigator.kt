/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.staticCompositionLocalOf
import ca.gosyer.data.models.Source
import ca.gosyer.ui.sources.browse.SourceScreen
import ca.gosyer.ui.sources.home.SourceHomeScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow

typealias SourcesNavigatorContent = @Composable (sourcesNavigator: SourcesNavigator) -> Unit

val LocalSourcesNavigator: ProvidableCompositionLocal<SourcesNavigator?> =
    staticCompositionLocalOf { null }

@Composable
fun SourcesNavigator(
    homeScreen: SourceHomeScreen,
    removeSource: (Source) -> Unit,
    selectSource: (Source) -> Unit,
    content: SourcesNavigatorContent = { CurrentSource() }
) {
    Navigator(homeScreen, autoDispose = false, onBackPressed = null) { navigator ->
        val sourcesNavigator = remember(navigator) {
            SourcesNavigator(navigator, homeScreen, removeSource, selectSource)
        }

        CompositionLocalProvider(LocalSourcesNavigator provides sourcesNavigator) {
            content(sourcesNavigator)
        }
    }
}

class SourcesNavigator internal constructor(
    private val navigator: Navigator,
    private val homeScreen: SourceHomeScreen,
    private val removeSource: (Source) -> Unit,
    private val selectSource: (Source) -> Unit,
    val stateHolder: SaveableStateHolder = navigator.stateHolder
) {

    fun remove(source: Source) {
        removeSource(source)
        navigator replaceAll homeScreen
        stateHolder.removeState(source.id)
    }

    fun select(source: Source) {
        selectSource(source)
        navigator replaceAll SourceScreen(source)
    }

    var current
        get() = navigator.lastItem
        set(value) = navigator replaceAll value
}

@Composable
fun CurrentSource() {
    val sourcesNavigator = LocalSourcesNavigator.currentOrThrow
    val currentSource = sourcesNavigator.current

    sourcesNavigator.stateHolder.SaveableStateProvider(currentSource.key) {
        currentSource.Content()
    }
}
