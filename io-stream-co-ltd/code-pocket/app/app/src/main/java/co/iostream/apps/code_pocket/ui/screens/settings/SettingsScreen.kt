package co.iostream.apps.code_pocket.ui.screens.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import co.iostream.apps.code_pocket.BuildConfig
import co.iostream.apps.code_pocket.R
import co.iostream.apps.code_pocket.components.HeaderBar
import co.iostream.apps.code_pocket.ui.theme.*
import co.iostream.apps.code_pocket.utils.AppUtils
import co.iostream.apps.code_pocket.utils.LocaleUtils
import java.time.Year
import java.util.*

data class SettingItem(val icon: Int, val title: String, val uri: String)

data class FeaturedItem(val icon: Int, val name: String, val description: String)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    val privacyItems = listOf(
        SettingItem(
            title = stringResource(id = R.string.privacy),
            icon = R.drawable.baseline_privacy_tip_24,
            uri = "https://www.iostream.vn/io/io-apps-privacy-policy-D13wF2"
        )
    )

    val miscItems = listOf(
        SettingItem(
            title = stringResource(id = R.string.rate_app),
            icon = R.drawable.baseline_thumb_up_24,
            uri = "market://details?id=${context.packageName}"
        ),
        SettingItem(
            title = stringResource(id = R.string.help_and_support),
            icon = R.drawable.baseline_mode_comment_24,
            uri = "https://www.iostream.vn/contact"
        ),
    )

    val promotionItems = listOf(
        SettingItem(
            title = stringResource(id = R.string.more_apps),
            icon = R.drawable.baseline_workspaces_24,
            uri = "https://www.iostream.vn/apps"
        )
    )

    Scaffold(topBar = {
        HeaderBar(left = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(Icons.Filled.ArrowBack, null, modifier = Modifier.size(24.dp))
            }
        }, title = stringResource(id = R.string.settings))
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .scrollable(state = scrollState, orientation = Orientation.Vertical)
        ) {
            val availableLanguages = LocaleUtils.getAvailableLocales().map { it.language }
            val availableSupportedLanguages = AppUtils.getSupportedLanguages()
            val supportedLanguages =
                availableLanguages.intersect(availableSupportedLanguages.toSet())

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(0.dp))
                    .background(Color(0x11ffffff))
                    .padding(vertical = 15.dp)
                    .padding(start = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val vector = ImageVector.vectorResource(id = R.drawable.icon)
                    val painter = rememberVectorPainter(image = vector)

                    Canvas(
                        modifier = Modifier.size(35.dp)
                    ) {
                        with(painter) {
                            draw(
                                size = Size(
                                    width = 35.dp.toPx(), height = 35.dp.toPx()
                                )
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(start = 15.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME}", style = TextStyle(
                                color = Foreground2
                            )
                        )
                    }
                }
            }

//            if (supportedLanguages.isNotEmpty()) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(0.dp))
//                        .padding(vertical = 5.dp)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .clickable(indication = null,
//                                interactionSource = MutableInteractionSource(),
//                                onClick = { navController.navigate(SettingsNavigationGraph.LANGUAGE) })
//                            .fillMaxWidth()
//                            .padding(horizontal = 15.dp)
//                            .height(50.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Column(modifier = Modifier.weight(6f)) {
//                            Text(
//                                text = stringResource(id = R.string.language),
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.SemiBold
//                            )
//                        }
//                        Column {
//                            Icon(
//                                painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
//                                "icon",
//                                modifier = Modifier.size(20.dp)
//                            )
//                        }
//                    }
//                }
//            }
//
//            Divider(thickness = 1.dp)

            Column(modifier = Modifier.weight(1f)) {
                LazyColumn {
                    itemsIndexed(miscItems, key = { _, item -> item.uri }) { _, item ->
                        val intent = remember {
                            Intent(
                                Intent.ACTION_VIEW, Uri.parse(item.uri)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(0.dp))
                                .clickable(indication = null,
                                    interactionSource = MutableInteractionSource(),
                                    onClick = {
                                        context.startActivity(intent)
                                    })
                                .padding(15.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painterResource(item.icon),
                                    null,
                                    modifier = Modifier.size(28.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 15.dp)
                                ) {
                                    Text(
                                        text = item.title, fontSize = 14.sp
                                    )
                                    Icon(
                                        painterResource(R.drawable.baseline_arrow_forward_ios_24),
                                        null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }


                LazyColumn {
                    itemsIndexed(privacyItems, key = { _, item -> item.uri }) { _, item ->
                        val intent = remember {
                            Intent(
                                Intent.ACTION_VIEW, Uri.parse(item.uri)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(0.dp))
                                .clickable(indication = null,
                                    interactionSource = MutableInteractionSource(),
                                    onClick = {
                                        context.startActivity(intent)
                                    })
                                .padding(15.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(item.icon),
                                    "icon",
                                    modifier = Modifier.size(28.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 15.dp)
                                ) {
                                    Text(
                                        text = item.title, fontSize = 14.sp
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
                }


                LazyColumn {
                    itemsIndexed(promotionItems, key = { _, item -> item.uri }) { _, item ->
                        val intent = remember {
                            Intent(
                                Intent.ACTION_VIEW, Uri.parse(item.uri)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(color = Color.Transparent)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(0.dp))
                                .clickable(
                                    indication = null,
                                    interactionSource = MutableInteractionSource(),
                                    onClick = { context.startActivity(intent) },
                                )
                                .padding(15.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(item.icon),
                                    "icon",
                                    modifier = Modifier.size(30.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 15.dp)
                                ) {
                                    Text(
                                        text = item.title, fontSize = 14.sp
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
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val intent = remember {
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://www.iostream.vn/")
                    )
                }
                Text(text = "©${stringResource(id = R.string.company_full_name)}, ${Year.now().value}",
                    modifier = Modifier.clickable { context.startActivity(intent) })
                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }
}