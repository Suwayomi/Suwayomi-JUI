/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("KDocUnresolvedReference")

package ca.gosyer.jui.uicore.insets

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable

/**
 * For the [WindowInsetsCompat.Type.captionBar].
 */
expect val WindowInsets.Companion.captionBar: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * For the [WindowInsetsCompat.Type.displayCutout]. This insets represents the area that the
 * display cutout (e.g. for camera) is and important content should be excluded from.
 */
expect val WindowInsets.Companion.displayCutout: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * For the [WindowInsetsCompat.Type.ime]. On API level 23 (M) and above, the soft keyboard can be
 * detected and [ime] will update when it shows. On API 30 (R) and above, the [ime] insets will
 * animate synchronously with the actual IME animation.
 *
 * Developers should set `android:windowSoftInputMode="adjustResize"` in their
 * `AndroidManifest.xml` file and call `WindowCompat.setDecorFitsSystemWindows(window, false)`
 * in their [android.app.Activity.onCreate].
 */
expect val WindowInsets.Companion.ime: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * For the [WindowInsetsCompat.Type.mandatorySystemGestures]. These insets represents the
 * space where system gestures have priority over application gestures.
 */
expect val WindowInsets.Companion.mandatorySystemGestures: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * For the [WindowInsetsCompat.Type.navigationBars]. These insets represent where
 * system UI places navigation bars. Interactive UI should avoid the navigation bars
 * area.
 */
expect val WindowInsets.Companion.navigationBars: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * For the [WindowInsetsCompat.Type.statusBars].
 */
expect val WindowInsets.Companion.statusBars: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * For the [WindowInsetsCompat.Type.systemBars].
 */
expect val WindowInsets.Companion.systemBars: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * For the [WindowInsetsCompat.Type.systemGestures].
 */
expect val WindowInsets.Companion.systemGestures: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * For the [WindowInsetsCompat.Type.tappableElement].
 */
expect val WindowInsets.Companion.tappableElement: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * The insets for the curved areas in a waterfall display.
 */
expect val WindowInsets.Companion.waterfall: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * The insets that include areas where content may be covered by other drawn content.
 * This includes all [system bars][systemBars], [display cutout][displayCutout], and
 * [soft keyboard][ime].
 */
expect val WindowInsets.Companion.safeDrawing: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * The insets that include areas where gestures may be confused with other input,
 * including [system gestures][systemGestures],
 * [mandatory system gestures][mandatorySystemGestures],
 * [rounded display areas][waterfall], and [tappable areas][tappableElement].
 */
expect val WindowInsets.Companion.safeGestures: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * The insets that include all areas that may be drawn over or have gesture confusion,
 * including everything in [safeDrawing] and [safeGestures].
 */
expect val WindowInsets.Companion.safeContent: WindowInsets
    @Composable
    @NonRestartableComposable
    get

/**
 * The insets that the [WindowInsetsCompat.Type.captionBar] will consume if shown.
 * If it cannot be shown then this will be empty.
 */
@ExperimentalLayoutApi
expect val WindowInsets.Companion.captionBarIgnoringVisibility: WindowInsets
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get

/**
 * The insets that [WindowInsetsCompat.Type.navigationBars] will consume if shown.
 * These insets represent where system UI places navigation bars. Interactive UI should
 * avoid the navigation bars area. If navigation bars cannot be shown, then this will be
 * empty.
 */
@ExperimentalLayoutApi
expect val WindowInsets.Companion.navigationBarsIgnoringVisibility: WindowInsets
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get

/**
 * The insets that [WindowInsetsCompat.Type.statusBars] will consume if shown.
 * If the status bar can never be shown, then this will be empty.
 */
@ExperimentalLayoutApi
expect val WindowInsets.Companion.statusBarsIgnoringVisibility: WindowInsets
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get

/**
 * The insets that [WindowInsetsCompat.Type.systemBars] will consume if shown.
 *
 * If system bars can never be shown, then this will be empty.
 */
@ExperimentalLayoutApi
expect val WindowInsets.Companion.systemBarsIgnoringVisibility: WindowInsets
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get

/**
 * The insets that [WindowInsetsCompat.Type.tappableElement] will consume if active.
 *
 * If there are never tappable elements then this is empty.
 */
@ExperimentalLayoutApi
expect val WindowInsets.Companion.tappableElementIgnoringVisibility: WindowInsets
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get

/**
 * `true` when the [caption bar][captionBar] is being displayed, irrespective of
 * whether it intersects with the Window.
 */
@ExperimentalLayoutApi
expect val WindowInsets.Companion.isCaptionBarVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get

/**
 * `true` when the [soft keyboard][ime] is being displayed, irrespective of
 * whether it intersects with the Window.
 */
@ExperimentalLayoutApi
expect val WindowInsets.Companion.isImeVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get

/**
 * `true` when the [statusBars] are being displayed, irrespective of
 * whether they intersects with the Window.
 */
@ExperimentalLayoutApi
expect val WindowInsets.Companion.areStatusBarsVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get

/**
 * `true` when the [navigationBars] are being displayed, irrespective of
 * whether they intersects with the Window.
 */
@ExperimentalLayoutApi
expect val WindowInsets.Companion.areNavigationBarsVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get

/**
 * `true` when the [systemBars] are being displayed, irrespective of
 * whether they intersects with the Window.
 */
@ExperimentalLayoutApi
expect val WindowInsets.Companion.areSystemBarsVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get

/**
 * `true` when the [tappableElement] is being displayed, irrespective of
 * whether they intersects with the Window.
 */
@ExperimentalLayoutApi
expect val WindowInsets.Companion.isTappableElementVisible: Boolean
    @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
    @ExperimentalLayoutApi
    @Composable
    @NonRestartableComposable
    get
