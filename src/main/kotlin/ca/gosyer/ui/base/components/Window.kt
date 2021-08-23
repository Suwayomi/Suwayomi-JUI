package ca.gosyer.ui.base.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.FrameWindowScope
import javax.imageio.ImageIO

@Composable
fun FrameWindowScope.setIcon() {
    DisposableEffect(Unit) {
        window.iconImage = this::class.java.classLoader.getResourceAsStream("icon.png")!!.use {
            ImageIO.read(it)
        }
        onDispose { }
    }
}
