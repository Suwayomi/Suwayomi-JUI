/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.gosyer.jui.core.lang.bytesIntoHumanReadable
import ca.gosyer.jui.core.lang.launchIO
import ca.gosyer.jui.domain.updates.service.UpdatePreferences
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.ChapterCache
import ca.gosyer.jui.ui.base.ImageCache
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.PreferenceRow
import ca.gosyer.jui.ui.base.prefs.SwitchPreference
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.ui.viewModel
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.insets.navigationBars
import ca.gosyer.jui.uicore.insets.statusBars
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging
import kotlin.time.Duration.Companion.seconds

class SettingsAdvancedScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel { settingsAdvancedViewModel() }
        SettingsAdvancedScreenContent(
            updatesEnabled = vm.updatesEnabled,
            imageCacheSize = vm.imageCacheSize.collectAsState().value,
            clearImageCache = vm::clearImageCache,
            chapterCacheSize = vm.chapterCacheSize.collectAsState().value,
            clearChapterCache = vm::clearChapterCache
        )
    }
}

class SettingsAdvancedViewModel @Inject constructor(
    updatePreferences: UpdatePreferences,
    private val imageCache: ImageCache,
    private val chapterCache: ChapterCache,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
    val updatesEnabled = updatePreferences.enabled().asStateFlow()

    val imageCacheSize = flow {
        while (currentCoroutineContext().isActive) {
            emit(imageCache.size.bytesIntoHumanReadable())
            delay(1.seconds)
        }
    }.stateIn(scope, SharingStarted.Eagerly, "")

    val chapterCacheSize = flow {
        while (currentCoroutineContext().isActive) {
            emit(chapterCache.size.bytesIntoHumanReadable())
            delay(1.seconds)
        }
    }.stateIn(scope, SharingStarted.Eagerly, "")

    fun clearImageCache() {
        scope.launchIO {
            imageCache.clear()
        }
    }

    fun clearChapterCache() {
        scope.launchIO {
            chapterCache.clear()
        }
    }
    companion object {
        private val log = logging()
    }
}

@Composable
fun SettingsAdvancedScreenContent(
    updatesEnabled: PreferenceMutableStateFlow<Boolean>,
    imageCacheSize: String,
    clearImageCache: () -> Unit,
    chapterCacheSize: String,
    clearChapterCache: () -> Unit
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
            )
        ),
        topBar = {
            Toolbar(stringResource(MR.strings.settings_advanced_screen))
        }
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                contentPadding = WindowInsets.bottomNav.add(
                    WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom
                    )
                ).asPaddingValues()
            ) {
                item {
                    SwitchPreference(preference = updatesEnabled, title = stringResource(MR.strings.update_checker))
                }
                item {
                    Divider()
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.clear_image_cache),
                        subtitle = stringResource(MR.strings.clear_cache_sub, imageCacheSize),
                        onClick = clearImageCache
                    )
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.clear_chapter_cache),
                        subtitle = stringResource(MR.strings.clear_cache_sub, chapterCacheSize),
                        onClick = clearChapterCache
                    )
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .scrollbarPadding()
                    .windowInsetsPadding(
                        WindowInsets.bottomNav.add(
                            WindowInsets.navigationBars.only(
                                WindowInsetsSides.Bottom
                            )
                        )
                    )
            )
        }
    }
}
