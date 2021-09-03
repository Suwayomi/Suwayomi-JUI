package ca.gosyer.ui.base.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack

val LocalMenuController =
    compositionLocalOf<MenuController?> { null }

class MenuController(
    val backStack: BackStack<Route>,
    private val _sideMenuVisible: MutableState<Boolean> = mutableStateOf(true),
) {
    val sideMenuVisible by _sideMenuVisible

    fun openSideMenu() {
        _sideMenuVisible.value = true
    }
    fun closeSideMenu() {
        _sideMenuVisible.value = false
    }
}

@Composable
fun withMenuController(controller: MenuController, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalMenuController provides controller,
        content = content
    )
}
