/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("KDocUnresolvedReference", "unused")

package ca.gosyer.jui.uicore.insets

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Immutable
object EmptyWindowInsets : WindowInsets {
    override fun getBottom(density: Density): Int = 0
    override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int = 0
    override fun getRight(density: Density, layoutDirection: LayoutDirection): Int = 0
    override fun getTop(density: Density): Int = 0
}


/**
 * For the [WindowInsetsCompat.Type.captionBar].
 */
actual val WindowInsets.Companion.captionBar: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * For the [WindowInsetsCompat.Type.displayCutout]. This insets represents the area that the
 * display cutout (e.g. for camera) is and important content should be excluded from.
 */
actual val WindowInsets.Companion.displayCutout: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * For the [WindowInsetsCompat.Type.ime]. On API level 23 (M) and above, the soft keyboard can be
 * detected and [ime] will update when it shows. On API 30 (R) and above, the [ime] insets will
 * animate synchronously with the actual IME animation.
 *
 * Developers should set `android:windowSoftInputMode="adjustResize"` in their
 * `AndroidManifest.xml` file and call `WindowCompat.setDecorFitsSystemWindows(window, false)`
 * in their [android.app.Activity.onCreate].
 */
actual val WindowInsets.Companion.ime: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * For the [WindowInsetsCompat.Type.mandatorySystemGestures]. These insets represents the
 * space where system gestures have priority over application gestures.
 */
actual val WindowInsets.Companion.mandatorySystemGestures: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * For the [WindowInsetsCompat.Type.navigationBars]. These insets represent where
 * system UI places navigation bars. Interactive UI should avoid the navigation bars
 * area.
 */
actual val WindowInsets.Companion.navigationBars: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * For the [WindowInsetsCompat.Type.statusBars].
 */
actual val WindowInsets.Companion.statusBars: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * For the [WindowInsetsCompat.Type.systemBars].
 */
actual val WindowInsets.Companion.systemBars: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * For the [WindowInsetsCompat.Type.systemGestures].
 */
actual val WindowInsets.Companion.systemGestures: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * For the [WindowInsetsCompat.Type.tappableElement].
 */
actual val WindowInsets.Companion.tappableElement: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * The insets for the curved areas in a waterfall display.
 */
actual val WindowInsets.Companion.waterfall: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * The insets that include areas where content may be covered by other drawn content.
 * This includes all [system bars][systemBars], [display cutout][displayCutout], and
 * [soft keyboard][ime].
 */
actual val WindowInsets.Companion.safeDrawing: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * The insets that include areas where gestures may be confused with other input,
 * including [system gestures][systemGestures],
 * [mandatory system gestures][mandatorySystemGestures],
 * [rounded display areas][waterfall], and [tappable areas][tappableElement].
 */
actual val WindowInsets.Companion.safeGestures: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * The insets that include all areas that may be drawn over or have gesture confusion,
 * including everything in [safeDrawing] and [safeGestures].
 */
actual val WindowInsets.Companion.safeContent: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * The insets that the [WindowInsetsCompat.Type.captionBar] will consume if shown.
 * If it cannot be shown then this will be empty.
 */
@ExperimentalLayoutApi
actual val WindowInsets.Companion.captionBarIgnoringVisibility: WindowInsets
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * The insets that [WindowInsetsCompat.Type.navigationBars] will consume if shown.
 * These insets represent where system UI places navigation bars. Interactive UI should
 * avoid the navigation bars area. If navigation bars cannot be shown, then this will be
 * empty.
 */
@ExperimentalLayoutApi
actual val WindowInsets.Companion.navigationBarsIgnoringVisibility: WindowInsets
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * The insets that [WindowInsetsCompat.Type.statusBars] will consume if shown.
 * If the status bar can never be shown, then this will be empty.
 */
@ExperimentalLayoutApi
actual val WindowInsets.Companion.statusBarsIgnoringVisibility: WindowInsets
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * The insets that [WindowInsetsCompat.Type.systemBars] will consume if shown.
 *
 * If system bars can never be shown, then this will be empty.
 */
@ExperimentalLayoutApi
actual val WindowInsets.Companion.systemBarsIgnoringVisibility: WindowInsets
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * The insets that [WindowInsetsCompat.Type.tappableElement] will consume if active.
 *
 * If there are never tappable elements then this is empty.
 */
@ExperimentalLayoutApi
actual val WindowInsets.Companion.tappableElementIgnoringVisibility: WindowInsets
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get() = EmptyWindowInsets

/**
 * `true` when the [caption bar][captionBar] is being displayed, irrespective of
 * whether it intersects with the Window.
 */
@ExperimentalLayoutApi
actual val WindowInsets.Companion.isCaptionBarVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get() = false

/**
 * `true` when the [soft keyboard][ime] is being displayed, irrespective of
 * whether it intersects with the Window.
 */
@ExperimentalLayoutApi
actual val WindowInsets.Companion.isImeVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get() = false

/**
 * `true` when the [statusBars] are being displayed, irrespective of
 * whether they intersects with the Window.
 */
@ExperimentalLayoutApi
actual val WindowInsets.Companion.areStatusBarsVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get() = false

/**
 * `true` when the [navigationBars] are being displayed, irrespective of
 * whether they intersects with the Window.
 */
@ExperimentalLayoutApi
actual val WindowInsets.Companion.areNavigationBarsVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get() = false

/**
 * `true` when the [systemBars] are being displayed, irrespective of
 * whether they intersects with the Window.
 */
@ExperimentalLayoutApi
actual val WindowInsets.Companion.areSystemBarsVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get() = false
/**
 * `true` when the [tappableElement] is being displayed, irrespective of
 * whether they intersects with the Window.
 */
@ExperimentalLayoutApi
actual val WindowInsets.Companion.isTappableElementVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get() = false
