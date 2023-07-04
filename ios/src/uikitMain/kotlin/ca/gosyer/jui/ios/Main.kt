package ca.gosyer.jui.ios

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.window.Application
import ca.gosyer.jui.ui.base.theme.AppTheme
import ca.gosyer.jui.ui.main.MainMenu
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.Length
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.useContents
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import platform.Foundation.NSStringFromClass
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDelegateProtocol
import platform.UIKit.UIApplicationDelegateProtocolMeta
import platform.UIKit.UIApplicationMain
import platform.UIKit.UIResponder
import platform.UIKit.UIResponderMeta
import platform.UIKit.UIScreen
import platform.UIKit.UIWindow
import platform.UIKit.safeAreaInsets
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun main() {
    val args = emptyArray<String>()
    memScoped {
        val argc = args.size + 1
        val argv = (arrayOf("skikoApp") + args).map { it.cstr.ptr }.toCValues()
        autoreleasepool {
            UIApplicationMain(argc, argv, null, NSStringFromClass(SkikoAppDelegate))
        }
    }
}

class SkikoAppDelegate
    @OverrideInit
    constructor() : UIResponder(), UIApplicationDelegateProtocol {
        companion object : UIResponderMeta(), UIApplicationDelegateProtocolMeta

        private var _window: UIWindow? = null
        override fun window() = _window
        override fun setWindow(window: UIWindow?) {
            _window = window
        }

        private val context = ContextWrapper()

        private val appComponent = AppComponent.getInstance(context)

        init {
            appComponent.migrations.runMigrations()
            appComponent.appMigrations.runMigrations()

            appComponent.downloadService.init()
            appComponent.libraryUpdateService.init()
        }

        val uiHooks = appComponent.hooks

        override fun application(
            application: UIApplication,
            didFinishLaunchingWithOptions: Map<Any?, *>?,
        ): Boolean {
            window = UIWindow(frame = UIScreen.mainScreen.bounds).apply {
                val insets = safeAreaInsets.useContents {
                    WindowInsets(left.dp, top.dp, right.dp, bottom.dp)
                }

                rootViewController = Application("Tachidesk-JUI") {
                    CompositionLocalProvider(*uiHooks) {
                        AppTheme {
                            Box(Modifier.fillMaxSize().windowInsetsPadding(insets)) {
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
                makeKeyAndVisible()
            }
            return true
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
