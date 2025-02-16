package co.iostream.apps.code_pocket.navigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import co.iostream.apps.code_pocket.R
import co.iostream.apps.code_pocket.ui.theme.Foreground2

@Composable
fun BottomNavigator(navController: NavHostController) {
    BottomNavigation(
        modifier = Modifier,
        contentColor = Foreground2,
        backgroundColor = Color(0xFF17181C),
        elevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        BottomNavigationItem(icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_people_24),
                contentDescription = null
            )
        },
            label = { Text(stringResource(R.string.other_codes)) },
            selected = currentDestination?.hierarchy?.any { it.route == BottomNavigatorGraph.OTHERS } == true,
            onClick = {
                navController.navigate(BottomNavigatorGraph.OTHERS) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        BottomNavigationItem(icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_person_24),
                contentDescription = null
            )
        },
            label = { Text(stringResource(R.string.my_codes)) },
            selected = currentDestination?.hierarchy?.any { it.route == BottomNavigatorGraph.ME } == true,
            onClick = {
                navController.navigate(BottomNavigatorGraph.ME) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        BottomNavigationItem(icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_person_pin_24),
                contentDescription = null
            )
        },
            label = { Text(stringResource(R.string.user)) },
            selected = currentDestination?.hierarchy?.any { it.route == BottomNavigatorGraph.PROMOTION } == true,
            onClick = {
                navController.navigate(UserNavigatorGraph.USER)
            })
        BottomNavigationItem(icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_auto_awesome_24),
                contentDescription = null
            )
        },
            label = { Text(stringResource(R.string.promotion)) },
            selected = currentDestination?.hierarchy?.any { it.route == BottomNavigatorGraph.PROMOTION } == true,
            onClick = {
                navController.navigate(BottomNavigatorGraph.PROMOTION) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            })
    }
}