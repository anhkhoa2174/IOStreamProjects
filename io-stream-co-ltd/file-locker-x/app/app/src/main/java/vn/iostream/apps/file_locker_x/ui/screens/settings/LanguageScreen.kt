package vn.iostream.apps.file_locker_x.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import vn.iostream.apps.data.helper.LocaleUtils
import vn.iostream.apps.file_locker_x.R
import vn.iostream.apps.file_locker_x.ui.composables.TopBar
import vn.iostream.apps.file_locker_x.ui.theme.ThemeLighter
import vn.iostream.apps.file_locker_x.utils.AppUtils
import java.util.Locale

@Composable
private fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun LanguageScreen(navController: NavHostController) {
    val context = LocalContext.current

    val currentLocale = context.resources.configuration.locales.get(0)
    var supportedLocale by remember { mutableStateOf(emptyList<Locale>()) }
    var selectedLanguage by remember { mutableStateOf(currentLocale.language) }

    val availableLanguages = LocaleUtils.getAvailableLocales().map { it.language }
    val availableSupportedLanguages = AppUtils.getSupportedLanguages()
    val supportedLanguages = availableLanguages.intersect(availableSupportedLanguages.toSet())

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                supportedLocale =
                    supportedLanguages.filter { availableLanguages.contains(it) }.map {
                        Locale(it)
                    }
            }

            else -> {}
        }
    }

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
            ) {
                TopBar(
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer),
                    left = {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(id = R.string.language),
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            ),
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    right = {
                        Spacer(modifier = Modifier.size(48.dp))
                    },
                )
                Text(
                    text = stringResource(id = R.string.notice_changing_language),
                    style = TextStyle(fontSize = 14.sp, color = ThemeLighter),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 15.dp, top = 10.dp)
                )
                LazyColumn {
                    items(supportedLocale) { locale ->
                        val checkbox = locale.language == selectedLanguage
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .height(50.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(0.dp))
                                    .clickable(indication = null,
                                        interactionSource = MutableInteractionSource(),
                                        role = Role.Switch,
                                        onClick = {
                                            selectedLanguage = locale.language
                                            setLocale(context, locale)
                                        }),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = locale.getDisplayName(locale),
                                    style = TextStyle(fontSize = 14.sp)
                                )
                                RadioButton(selected = checkbox, onClick = {
                                    selectedLanguage = locale.language
                                    setLocale(context, locale)
                                })
                            }
                        }
                    }
                }
            }
        }
    )
}

@SuppressLint("ApplySharedPref")
private fun setLocale(context: Context, locale: Locale) {
    Locale.setDefault(locale)
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(locale)
    configuration.setLayoutDirection(locale)
    context.createConfigurationContext(configuration)
    val editor: SharedPreferences.Editor =
        context.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
    editor.putString("Selected_Language", locale.language)
    editor.commit()

    val packageManager: PackageManager = context.packageManager
    val intent: Intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
    val componentName: ComponentName = intent.component!!
    val restartIntent: Intent = Intent.makeRestartActivityTask(componentName)
    context.startActivity(restartIntent)
    Runtime.getRuntime().exit(0)
}