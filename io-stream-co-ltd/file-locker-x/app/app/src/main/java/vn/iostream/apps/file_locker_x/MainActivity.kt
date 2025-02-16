package vn.iostream.apps.file_locker_x

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.AndroidEntryPoint
import vn.iostream.apps.file_locker_x.ui.theme.IOTheme
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    finishAndRemoveTask()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storageRequestCode = 1656

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val getPermissionIntent = Intent()
                getPermissionIntent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startForResult.launch(getPermissionIntent)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    storageRequestCode
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            IOTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainApp()
                }
            }
        }

        MobileAds.initialize(this) {}
//        Adding test devices
        val testDeviceIds = listOf(
////            phu.nguyenduc
            "F9F813CC38E15FF9383417B304B4D3F5",
        )
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)
    }

    override fun attachBaseContext(newBase: Context) {
        val systemLang = newBase.resources.configuration.locales.get(0)
        val sharedPreferences = newBase.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString(
            "SelectedLanguage", systemLang.language
        )
        val locale = Locale(language!!)
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }
}
