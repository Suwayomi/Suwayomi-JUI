package ca.gosyer.ui.base.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.awt.ComposeWindow

val LocalComposeWindow = compositionLocalOf<ComposeWindow> { throw RuntimeException("ComposeWindow not initialized") }
