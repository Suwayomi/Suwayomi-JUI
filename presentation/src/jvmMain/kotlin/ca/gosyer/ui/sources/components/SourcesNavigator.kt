/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.staticCompositionLocalOf
import ca.gosyer.data.models.Source
import ca.gosyer.ui.sources.browse.SourceScreen
import ca.gosyer.ui.sources.globalsearch.GlobalSearchScreen
import ca.gosyer.ui.sources.home.SourceHomeScreen
import cafe.adriel.voyager.core.model.ScreenModelStore
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow

typealias SourcesNavigatorContent = @Composable (sourcesNavigator: SourcesNavigator) -> Unit

val LocalSourcesNavigator: ProvidableCompositionLocal<SourcesNavigator?> =
    staticCompositionLocalOf { null }

@Composable
fun SourcesNavigator(
    homeScreen: SourceHomeScreen,
    content: SourcesNavigatorContent = { CurrentSource() }
) {
    Navigator(homeScreen, autoDispose = false, onBackPressed = null) { navigator ->
        val sourcesNavigator = remember(navigator) {
            SourcesNavigator(navigator, homeScreen)
        }

        DisposableEffect(sourcesNavigator) {
            onDispose(sourcesNavigator::dispose)
        }

        CompositionLocalProvider(LocalSourcesNavigator provides sourcesNavigator) {
            content(sourcesNavigator)
        }
    }
}

sealed class SourceNavigatorScreen {
    object HomeScreen : SourceNavigatorScreen()
    object SearchScreen : SourceNavigatorScreen()
    data class SourceScreen(val source: Source) : SourceNavigatorScreen()
}

class SourcesNavigator internal constructor(
    private val navigator: Navigator,
    homeScreen: SourceHomeScreen,
    val stateHolder: SaveableStateHolder = navigator.stateHolder
) {
    private var screens = SnapshotStateMap<Long, Screen>().also { it[-1] = homeScreen }

    fun remove(source: Source) {
        navigator replaceAll screens[-1]!!
        screens.remove(source.id)?.let(::cleanup)
    }

    fun select(source: Source) {
        navigator replaceAll screens.getOrPut(source.id) { SourceScreen(source) }
    }

    fun open(source: Source, query: String? = null) {
        screens.remove(source.id)?.let(::cleanup)
        navigator replaceAll SourceScreen(source, query).also { screens[source.id] = it }
    }

    fun search(query: String) {
        screens[-2]?.let(::cleanup)
        navigator replaceAll GlobalSearchScreen(query).also { screens[-2] = it }
    }

    private fun cleanup(screen: Screen) {
        stateHolder.removeState(screen.key)
        ScreenModelStore.remove(screen)
    }

    fun goHome() {
        navigator replaceAll screens[-1]!!
    }

    fun goToSearch() {
        screens[-2]?.let { navigator replaceAll it }
    }

    fun dispose() {
        screens.forEach {
            cleanup(it.value)
        }
    }

    val tabs by derivedStateOf {
        buildList {
            add(SourceNavigatorScreen.HomeScreen)
            if (screens.containsKey(-2)) {
                add(SourceNavigatorScreen.SearchScreen)
            }
            screens.values.filterIsInstance<SourceScreen>().map {
                SourceNavigatorScreen.SourceScreen(it.source)
            }.let(::addAll)
        }
    }

    val current
        get() = navigator.lastItem
}

@Composable
fun CurrentSource() {
    val sourcesNavigator = LocalSourcesNavigator.currentOrThrow
    val currentSource = sourcesNavigator.current

    sourcesNavigator.stateHolder.SaveableStateProvider(currentSource.key) {
        currentSource.Content()
    }
}
