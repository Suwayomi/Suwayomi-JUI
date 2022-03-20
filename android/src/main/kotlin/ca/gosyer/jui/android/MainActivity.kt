package ca.gosyer.jui.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import ca.gosyer.jui.android.data.download.AndroidDownloadService
import ca.gosyer.jui.data.base.WebsocketService.Actions
import ca.gosyer.jui.ui.base.theme.AppTheme
import ca.gosyer.jui.ui.main.MainMenu

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = AppComponent.getInstance(applicationContext)
        if (savedInstanceState == null) {
            appComponent.dataComponent.migrations.runMigrations()
            appComponent.appMigrations.runMigrations()
        }

        AndroidDownloadService.start(this, Actions.START)

        val uiHooks = appComponent.uiComponent.getHooks()
        setContent {
            CompositionLocalProvider(*uiHooks) {
                AppTheme {
                    MainMenu()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AndroidDownloadService.stop(this)
    }
}
