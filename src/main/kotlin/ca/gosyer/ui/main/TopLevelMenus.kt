package ca.gosyer.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Store
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ca.gosyer.ui.extensions.openExtensionsMenu
import ca.gosyer.ui.library.openLibraryMenu
import ca.gosyer.ui.main.components.DownloadsExtraInfo
import ca.gosyer.ui.sources.openSourcesMenu
import com.github.zsoltk.compose.router.BackStack

enum class TopLevelMenus(
    val textKey: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val menu: Routes,
    val top: Boolean,
    val openInNewWindow: () -> Unit = {},
    val extraInfo: (@Composable () -> Unit)? = null
) {
    Library("location_library", Icons.Outlined.Book, Icons.Rounded.Book, Routes.Library, true, ::openLibraryMenu),
    Sources("location_sources", Icons.Outlined.Explore, Icons.Rounded.Explore, Routes.Sources, true, ::openSourcesMenu),
    Extensions("location_extensions", Icons.Outlined.Store, Icons.Rounded.Store, Routes.Extensions, true, ::openExtensionsMenu),
    Downloads("location_downloads", Icons.Outlined.Download, Icons.Rounded.Download, Routes.Downloads, false, extraInfo = { DownloadsExtraInfo() }),
    Settings("location_settings", Icons.Outlined.Settings, Icons.Rounded.Settings, Routes.Settings, false);

    fun isSelected(backStack: BackStack<Routes>) = backStack.elements.first() == menu
}
