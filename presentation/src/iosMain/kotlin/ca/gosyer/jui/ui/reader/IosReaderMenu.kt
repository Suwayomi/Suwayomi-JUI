/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow

class ReaderScreen(val chapterIndex: Int, val mangaId: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ReaderMenu(
            chapterIndex,
            mangaId,
            navigator::pop,
        )
    }
}

actual class ReaderLauncher(private val navigator: Navigator?) {
    actual fun launch(
        chapterIndex: Int,
        mangaId: Long,
    ) {
        navigator?.push(ReaderScreen(chapterIndex, mangaId))
    }

    @Composable
    actual fun Reader() {
    }
}

@Composable
actual fun rememberReaderLauncher(): ReaderLauncher {
    val navigator = LocalNavigator.current
    return remember(navigator) { ReaderLauncher(navigator) }
}
