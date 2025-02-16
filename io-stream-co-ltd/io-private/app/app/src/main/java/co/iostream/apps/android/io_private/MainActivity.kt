package co.iostream.apps.android.io_private

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import co.iostream.apps.android.io_private.ui.theme.IOTheme
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = IOConfig.DATA_STORE_NAME)
val LocalNavController = compositionLocalOf<NavHostController> {
    error("No LocalNavController provided")
}

/**
 * Find the closest Activity in a given Context.
 */
internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

fun <T> DataStore<Preferences>.getValueFlow(
    key: Preferences.Key<T>, defaultValue: T
): Flow<T> {
    return this.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[key] ?: defaultValue
    }
}

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val getPermissionIntent = Intent()
                getPermissionIntent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startForResult.launch(getPermissionIntent)
            }
        } else {
            val storageRequestCode = 3655

            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
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
                    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
                        MainApp()
                    }
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
