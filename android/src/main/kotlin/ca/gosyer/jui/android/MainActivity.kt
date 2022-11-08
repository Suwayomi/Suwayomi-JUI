package ca.gosyer.jui.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import ca.gosyer.jui.android.data.download.AndroidDownloadService
import ca.gosyer.jui.android.data.library.AndroidLibraryService
import ca.gosyer.jui.domain.base.WebsocketService.Actions
import ca.gosyer.jui.ui.base.theme.AppTheme
import ca.gosyer.jui.ui.main.MainMenu
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = AppComponent.getInstance(applicationContext)
        if (savedInstanceState == null) {
            appComponent.migrations.runMigrations()
            appComponent.appMigrations.runMigrations()
        }

        // Do not let the launcher create a new activity http://stackoverflow.com/questions/16283079
        if (!isTaskRoot) {
            finish()
            return
        }

        AndroidDownloadService.start(this, Actions.START)
        AndroidLibraryService.start(this, Actions.START)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val uiHooks = appComponent.hooks
        setContent {
            CompositionLocalProvider(*uiHooks) {
                AppTheme {
                    SetSystemUI()
                    MainMenu()
                }
            }
        }
    }

    @Composable
    fun SetSystemUI() {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        val primaryColor = MaterialTheme.colors.primarySurface

        DisposableEffect(systemUiController, useDarkIcons, primaryColor) {
            systemUiController.setStatusBarColor(
                color = primaryColor,
                darkIcons = useDarkIcons
            )
            systemUiController.setNavigationBarColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons,
                navigationBarContrastEnforced = false
            )

            onDispose {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AndroidDownloadService.stop(this)
        AndroidLibraryService.stop(this)
    }
}
