package co.iostream.apps.code_pocket

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.iostream.apps.code_pocket.ui.theme.IOTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cameraRequestCode = 617

        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.CAMERA), cameraRequestCode
            )
        }
        /*
        **HELLO**
         */

        setContent {
            IOTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    MainComposable()
                }
            }
        }
    }
}
