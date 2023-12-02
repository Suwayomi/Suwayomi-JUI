@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package ca.gosyer.jui.ios

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeUIViewController
import ca.gosyer.jui.ui.base.theme.AppTheme
import ca.gosyer.jui.ui.main.MainMenu
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.Length
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import platform.UIKit.UIViewController
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun initializeApplication(): UIViewController {
    val appComponent = AppComponent.getInstance(ContextWrapper())

    appComponent.migrations.runMigrations()
    appComponent.appMigrations.runMigrations()

    appComponent.downloadService.init()
    appComponent.libraryUpdateService.init()

    val uiHooks = appComponent.hooks
    val context = appComponent.context

    return ComposeUIViewController {
        CompositionLocalProvider(*uiHooks) {
            AppTheme {
                Box(Modifier.fillMaxSize()) {
                    MainMenu()
                    ToastOverlay(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 64.dp),
                        context = context,
                    )
                }
            }
        }
    }
}

@Composable
fun ToastOverlay(
    modifier: Modifier,
    context: ContextWrapper,
) {
    var toast by remember { mutableStateOf<Pair<String, Length>?>(null) }
    LaunchedEffect(Unit) {
        context.toasts
            .onEach {
                toast = it
            }
            .launchIn(this)
    }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(
                when (toast?.second) {
                    Length.SHORT -> 2.seconds
                    Length.LONG -> 5.seconds
                    else -> Duration.ZERO
                },
            )
            toast = null
        }
    }
    @Suppress("NAME_SHADOWING")
    Crossfade(
        toast?.first,
        modifier = modifier,
    ) { toast ->
        if (toast != null) {
            Card(
                Modifier.sizeIn(maxWidth = 200.dp),
                shape = CircleShape,
                backgroundColor = Color.DarkGray,
            ) {
                Text(
                    toast,
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
