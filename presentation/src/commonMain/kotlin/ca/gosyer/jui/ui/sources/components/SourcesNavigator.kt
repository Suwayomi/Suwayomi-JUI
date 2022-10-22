/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.staticCompositionLocalOf
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.sources.browse.SourceScreen
import ca.gosyer.jui.ui.sources.globalsearch.GlobalSearchScreen
import ca.gosyer.jui.ui.sources.home.SourceHomeScreen
import cafe.adriel.voyager.core.lifecycle.DisposableEffectIgnoringConfiguration
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.collections.immutable.toImmutableList

typealias SourcesNavigatorContent = @Composable (sourcesNavigator: SourcesNavigator) -> Unit

val LocalSourcesNavigator: ProvidableCompositionLocal<SourcesNavigator?> =
    staticCompositionLocalOf { null }

@Composable
fun SourcesNavigator(
    homeScreenHolder: StableHolder<SourceHomeScreen>,
    content: SourcesNavigatorContent = { CurrentSource() }
) {
    Navigator(
        homeScreenHolder.item,
        onBackPressed = null,
        disposeBehavior = NavigatorDisposeBehavior(
            disposeSteps = false,
            disposeNestedNavigators = false
        )
    ) { navigator ->
        val sourcesNavigator = rememberNavigator(navigator, homeScreenHolder.item)

        DisposableEffectIgnoringConfiguration(sourcesNavigator) {
            onDispose(sourcesNavigator::dispose)
        }

        SourceNavigatorDisposableEffect(sourcesNavigator)

        CompositionLocalProvider(LocalSourcesNavigator provides sourcesNavigator) {
            content(sourcesNavigator)
        }
    }
}

private val disposableEvents: Set<StackEvent> =
    setOf(StackEvent.Pop, StackEvent.Replace)

@Composable
private fun rememberNavigator(
    parent: Navigator,
    homeScreen: SourceHomeScreen
): SourcesNavigator {
    return rememberSaveable(saver = navigatorSaver(parent, homeScreen)) {
        SourcesNavigator(parent, homeScreen)
    }
}

private fun navigatorSaver(
    parent: Navigator,
    homeScreen: SourceHomeScreen
): Saver<SourcesNavigator, Any> =
    mapSaver(
        save = { navigator -> navigator.screens.mapKeys { it.toString() } },
        restore = { items ->
            SourcesNavigator(
                parent,
                homeScreen,
                SnapshotStateMap<Long, Screen>().also { map ->
                    map.putAll(items.map { it.key.toLong() to (it.value as Screen) })
                },
            )
        }
    )

@Composable
private fun SourceNavigatorDisposableEffect(
    navigator: SourcesNavigator
) {
    val currentScreen = navigator.current

    DisposableEffectIgnoringConfiguration(currentScreen.key) {
        onDispose {
            if (
                navigator.lastEvent in disposableEvents &&
                currentScreen !is SourceScreen &&
                currentScreen !is SourceHomeScreen &&
                currentScreen !is GlobalSearchScreen
            ) {
                navigator.dispose(currentScreen)
                navigator.clearEvent()
            }
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
    val screens: SnapshotStateMap<Long, Screen> = SnapshotStateMap<Long, Screen>()
        .also { it[-1] = homeScreen },
) {

    fun remove(source: Source) {
        navigator replaceAll screens[-1]!!
        screens.remove(source.id)?.let(this::dispose)
    }

    fun select(source: Source) {
        navigator replaceAll screens.getOrPut(source.id) { SourceScreen(source) }
    }

    fun open(source: Source, query: String? = null) {
        screens.remove(source.id)?.let(this::dispose)
        navigator replaceAll SourceScreen(source, query).also { screens[source.id] = it }
    }

    fun search(query: String) {
        screens[-2]?.let(this::dispose)
        navigator replaceAll GlobalSearchScreen(query).also { screens[-2] = it }
    }

    @Composable
    fun saveableState(
        key: String,
        screen: Screen,
        content: @Composable () -> Unit
    ) {
        navigator.saveableState(key, screen, content)
    }

    fun dispose(screen: Screen) {
        navigator.dispose(screen)
    }

    fun goHome() {
        navigator replaceAll screens[-1]!!
    }

    fun goToSearch() {
        screens[-2]?.let { navigator replaceAll it }
    }

    fun dispose() {
        screens.forEach {
            dispose(it.value)
        }
        navigator.clearEvent()
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
        }.toImmutableList()
    }

    fun clearEvent() = navigator.clearEvent()

    val current
        get() = navigator.lastItem
    val lastEvent
        get() = navigator.lastEvent
}

@Composable
fun CurrentSource() {
    val sourcesNavigator = LocalSourcesNavigator.currentOrThrow
    val currentSource = sourcesNavigator.current

    sourcesNavigator.saveableState("sources", currentSource) {
        currentSource.Content()
    }
}
