package app.maximus

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import app.maximus.core.app.AppServices
import app.maximus.ui.navigation.MaximusNavHost
import app.maximus.ui.theme.MaximusTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** AppCompatActivity (not plain ComponentActivity) so that per-app locales work on API 29-32. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var services: AppServices

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            MaximusTheme {
                MaximusNavHost(services = services)
            }
        }
    }

    /**
     * Leaving the foreground locks the vault. Changing configuration (rotation) is not leaving,
     * and neither is the biometric prompt, which the vault only uses while locked anyway.
     */
    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) services.vaultSession.lock()
    }
}
