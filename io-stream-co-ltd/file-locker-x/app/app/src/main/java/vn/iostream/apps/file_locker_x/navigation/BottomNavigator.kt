package vn.iostream.apps.file_locker_x.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import vn.iostream.apps.file_locker_x.R

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: Int,
)

val bottomNavItems = listOf(
    BottomNavItem(
        name = "Home",
        route = ProcessingNavigatorGraph.SINGLE,
        icon = R.drawable.baseline_home_24,
    ),
    BottomNavItem(
        name = "Settings",
        route = SettingsNavigatorGraph.SETTINGS,
        icon = R.drawable.baseline_settings_24,
    ),
)

@Composable
fun ContentBottomBar(
    navController: NavHostController
) {
    val customModifier = Modifier
        .fillMaxWidth()
        .background(
            color = Color.Transparent
        )
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawWithCache {
            val path = Path()
            path.addRect(
                Rect(
                    topLeft = Offset.Zero,
                    bottomRight = Offset(size.width, size.height)
                )
            )
            onDrawWithContent {
                clipPath(path) {
                    // this draws the actual image - if you don't call drawContent, it wont
                    // render anything
                    this@onDrawWithContent.drawContent()
                }
                val dotSize = 90f
                // Clip a white border for the content
                drawCircle(
                    Color.Black,
                    radius = dotSize,
                    center = Offset(
                        x = size.width / 2,
                        y = 0f
                    ),
                    blendMode = BlendMode.Clear
                )
            }
        }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = customModifier.height(60.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigate(item.route) },
                label = {},
                icon = {
                    Icon(
                        painterResource(id = item.icon),
                        contentDescription = "${item.name} Icon",
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}