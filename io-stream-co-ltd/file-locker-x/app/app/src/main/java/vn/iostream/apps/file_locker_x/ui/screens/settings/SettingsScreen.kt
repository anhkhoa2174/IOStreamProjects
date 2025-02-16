package vn.iostream.apps.file_locker_x.ui.screens.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import vn.iostream.apps.data.helper.LocaleUtils
import vn.iostream.apps.file_locker_x.BuildConfig
import vn.iostream.apps.file_locker_x.R
import vn.iostream.apps.file_locker_x.navigation.ContentBottomBar
import vn.iostream.apps.file_locker_x.navigation.MiscNavigatorGraph
import vn.iostream.apps.file_locker_x.navigation.SettingsNavigatorGraph
import vn.iostream.apps.file_locker_x.ui.composables.FloatButton
import vn.iostream.apps.file_locker_x.ui.composables.TopBar
import vn.iostream.apps.file_locker_x.ui.theme.Foreground2
import vn.iostream.apps.file_locker_x.utils.AppUtils
import vn.iostream.apps.file_locker_x.viewmodels.FileManagerViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Year

private data class SettingItem(val icon: Int, val title: String, val url: String)

data class FeaturedItem(val icon: Int, val name: String, val description: String)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(navController: NavHostController, fileManagerViewModel: FileManagerViewModel) {
    val context = LocalContext.current

    val addFilesVisible = fileManagerViewModel.addFilesVisible.collectAsState()

    val miscItems = listOf(
        SettingItem(
            title = stringResource(id = R.string.rate_app),
            icon = R.drawable.baseline_thumb_up_24,
            url = "market://details?id=${context.packageName}"
        ),
        SettingItem(
            title = stringResource(id = R.string.help_and_support),
            icon = R.drawable.baseline_mode_comment_24,
            url = "https://www.iostream.co/contact"
        ),
        SettingItem(
            title = stringResource(id = R.string.how_to_use),
            icon = R.drawable.baseline_question_mark_24,
            url = "https://www.iostream.co/io/how-to-use-file-locker-x-7uy38"
        ),
    )

    val promotionItems = listOf(
        SettingItem(
            title = stringResource(id = R.string.more_apps),
            icon = R.drawable.baseline_workspaces_24,
            url = "https://www.iostream.co/apps"
        )
    )

    val privacyItems = listOf(
        SettingItem(
            title = stringResource(id = R.string.privacy),
            icon = R.drawable.baseline_privacy_tip_24,
            url = "https://www.iostream.co/io/io-apps-privacy-policy-D13wF2"
        )
    )

    Scaffold(
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                TopBar(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer),
                    left = {
                        Spacer(modifier = Modifier.size(48.dp))
                    },
                    title = {
                        Text(
                            text = stringResource(id = R.string.settings),
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
                    })
                SettingContent(
                    context = context,
                    navController = navController,
                    privacyItems = privacyItems,
                    promotionItems = promotionItems,
                    miscItems = miscItems
                )
            }
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.Transparent) {
                ContentBottomBar(navController = navController)
            }
        },
        floatingActionButton = {
            if (addFilesVisible.value) {
                FloatButton(
                    fileManagerViewModel = fileManagerViewModel,
                    modifier = Modifier.offset(y = 55.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    )
}

@Composable
private fun SettingContent(
    context: Context,
    navController: NavHostController,
    privacyItems: List<SettingItem>,
    promotionItems: List<SettingItem>,
    miscItems: List<SettingItem>,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .scrollable(state = scrollState, orientation = Orientation.Vertical)
    ) {
        val availableLanguages = LocaleUtils.getAvailableLocales().map { it.language }
        val availableSupportedLanguages = AppUtils.getSupportedLanguages()
        val supportedLanguages = availableLanguages.intersect(availableSupportedLanguages.toSet())

        AppBanner(
            appName = stringResource(id = R.string.app_name), context = context
        )

        Divider(thickness = 1.dp)

        Column(modifier = Modifier.weight(1f)) {
            if (supportedLanguages.isNotEmpty()) {
                SettingRowNotIcon(navController)
            }

            Divider(thickness = 1.dp)

            val settingItems: List<SettingItem> = miscItems + promotionItems + privacyItems
            settingItems.forEach { item ->
                SettingRow(context = context, navController, item = item)
            }
        }
    }
}

@Composable
private fun AppBanner(
    appName: String, context: Context
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .background(Color(0x11ffffff))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val vector = ImageVector.vectorResource(id = R.drawable.icon)
            val painter = rememberVectorPainter(image = vector)

            Canvas(
                modifier = Modifier.size(55.dp)
            ) {
                with(painter) {
                    draw(
                        size = Size(width = 55.dp.toPx(), height = 55.dp.toPx())
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = appName,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = TextStyle(color = Foreground2, fontSize = 10.sp),
                    modifier = Modifier.absolutePadding(bottom = 2.dp)
                )

                Row {
                    val intent = remember {
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse("https://www.iostream.co/")
                        )
                    }
                    Text(text = "©${stringResource(id = R.string.company_full_name)}, ${Year.now().value}",
                        style = TextStyle(fontSize = 12.sp),
                        modifier = Modifier.clickable { context.startActivity(intent) })
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    context: Context,
    navController: NavHostController,
    item: SettingItem,
    onClick: (() -> Unit)? = null
) {
    val intent = remember {
        Intent(
            Intent.ACTION_VIEW, Uri.parse(item.url)
        )
    }
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .clickable(indication = null, interactionSource = interactionSource, onClick = {
                onClick ?: navController.navigate(
                    "${MiscNavigatorGraph.WEBVIEW}/${
                        URLEncoder.encode(
                            item.url, StandardCharsets.UTF_8.toString()
                        )
                    }"
                )
            })
            .padding(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(item.icon), "icon", modifier = Modifier.size(28.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp)
            ) {
                Text(
                    text = item.title, style = TextStyle(fontSize = 14.sp)
                )

                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_forward_ios_24),
                    "icon",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingRowNotIcon(
    navController: NavHostController, onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .clickable(indication = null,
                interactionSource = interactionSource,
                onClick = { onClick ?: navController.navigate(SettingsNavigatorGraph.LANGUAGES) })
            .padding(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(6f)) {
                Text(
                    text = stringResource(id = R.string.language),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
            }

            Column {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                    "icon",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
