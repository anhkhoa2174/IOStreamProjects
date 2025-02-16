package vn.iostream.apps.file_locker_x.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import vn.iostream.apps.file_locker_x.R
import vn.iostream.apps.file_locker_x.ui.composables.TopBar

@Composable
fun PromotionScreen(navController: NavHostController) {
    val featuredApps = emptyList<FeaturedItem>()

    Scaffold(
        topBar = {
            TopBar(
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer),
                left = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.more_apps),
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
                }
            )
        },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    items(items = featuredApps, key = { it.name }) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(color = Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row {
                                    Image(
                                        painter = painterResource(it.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp)
                                    ) {
                                        Text(
                                            text = it.name,
                                            style = TextStyle(
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                Row {
                                    Text(
                                        text = it.description,
                                        style = TextStyle(fontSize = 14.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}