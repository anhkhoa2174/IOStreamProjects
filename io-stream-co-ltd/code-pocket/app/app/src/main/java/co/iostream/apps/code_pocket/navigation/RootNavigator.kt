package co.iostream.apps.code_pocket.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import co.iostream.apps.code_pocket.components.BarcodeScanner
import co.iostream.apps.code_pocket.components.BarcodeScannerScreen
import co.iostream.apps.code_pocket.domain.models.CodeItem
import co.iostream.apps.code_pocket.domain.models.CodeItemLabel
import co.iostream.apps.code_pocket.domain.models.CodeItemZone
import co.iostream.apps.code_pocket.ui.screens.AddScreen
import co.iostream.apps.code_pocket.ui.screens.EditScreen
import co.iostream.apps.code_pocket.ui.screens.MainScreen
import co.iostream.apps.code_pocket.ui.screens.QRScreen
import co.iostream.apps.code_pocket.ui.screens.TrashScreen
import co.iostream.apps.code_pocket.ui.screens.UserScreen
import co.iostream.apps.code_pocket.ui.screens.settings.LanguageScreen
import co.iostream.apps.code_pocket.ui.screens.settings.SettingsScreen
import co.iostream.apps.code_pocket.viewmodels.MainViewModel


object OthersNavigatorGraph {
    const val MAIN = "others_main"
    const val ADD = "others_add"
    const val EDIT = "others_edit"
    const val SCAN = "others_scan"
}

object MeNavigatorGraph {
    const val MAIN = "me_main"
    const val ADD = "me_add"
    const val EDIT = "me_edit"
    const val SCAN = "me_scan"
}

object PromotionNavigatorGraph {
    const val MAIN = "promotion_main"
}

object BottomNavigatorGraph {
    const val OTHERS = "bottom_others"
    const val ME = "bottom_me"
    const val PROMOTION = "bottom_promotion"
}
object TrashBottomNavigatorGraph {
    const val OTHERS = "bottom_others_trash"
    const val ME = "bottom_me_trash"
    const val PROMOTION = "bottom_promotion_trash"
}
object UserNavigatorGraph {
    const val USER = "user_profile"
}
object SettingsNavigatorGraph {
    const val SETTINGS = "settings_settings"
    const val LANGUAGE = "settings_language"
}

object RootNavigatorGraph {
    const val BOTTOM = "root_bottom"
    const val SETTINGS = "root_settings"
}

@Composable
fun RootNavigator(navController: NavHostController) {
    val mainViewModel: MainViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = RootNavigatorGraph.BOTTOM,
    ) {
        navigation(
            route = RootNavigatorGraph.BOTTOM, startDestination = BottomNavigatorGraph.ME
        ) {
            navigation(
                route = BottomNavigatorGraph.OTHERS, startDestination = OthersNavigatorGraph.MAIN
            ) {
                composable(route = OthersNavigatorGraph.MAIN) {
                    MainScreen(navController, CodeItemZone.OTHERS, mainViewModel)
                }
                composable(route = OthersNavigatorGraph.ADD) {
                    AddScreen(navController, CodeItemZone.OTHERS, mainViewModel)
                }
                composable(route = OthersNavigatorGraph.SCAN) {
                    QRScreen(navController, CodeItemZone.OTHERS, mainViewModel)
                }
                composable(route = TrashBottomNavigatorGraph.OTHERS){
                    TrashScreen(navController, CodeItemZone.OTHERS, mainViewModel)
                }
                composable(route = UserNavigatorGraph.USER){
                    UserScreen(navController, CodeItemZone.OTHERS, mainViewModel)
                }
                composable(
                    route = OthersNavigatorGraph.EDIT + "/{itemId}",
                    arguments = listOf(navArgument("itemId") {
                        type = NavType.LongType
                    })
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getLong("itemId")
                    EditScreen(navController, CodeItemZone.OTHERS, itemId)
                }
            }

            navigation(
                route = BottomNavigatorGraph.ME, startDestination = MeNavigatorGraph.MAIN
            ) {
                composable(route = MeNavigatorGraph.MAIN) {
                    MainScreen(navController, CodeItemZone.ME, mainViewModel)
                }
                composable(route = MeNavigatorGraph.ADD) {
                    AddScreen(navController, CodeItemZone.ME, mainViewModel)
                }
                composable(route = MeNavigatorGraph.SCAN) {
                    QRScreen(navController, CodeItemZone.ME, mainViewModel)
                }
                composable(route = TrashBottomNavigatorGraph.ME) {
                    TrashScreen(navController, CodeItemZone.ME, mainViewModel)
                }
                composable(route = UserNavigatorGraph.USER){
                    UserScreen(navController, CodeItemZone.ME, mainViewModel)
                }
                composable(
                    route = MeNavigatorGraph.EDIT + "/{itemId}",
                    arguments = listOf(navArgument("itemId") {
                        type = NavType.LongType
                    })
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getLong("itemId")
                    EditScreen(navController, CodeItemZone.ME, itemId)
                }
            }

            navigation(
                route = BottomNavigatorGraph.PROMOTION,
                startDestination = PromotionNavigatorGraph.MAIN
            ) {
                composable(route = PromotionNavigatorGraph.MAIN) {
                    MainScreen(navController, CodeItemZone.PROMOTION, mainViewModel)
                }
                composable(route = TrashBottomNavigatorGraph.PROMOTION){
                    TrashScreen(navController, CodeItemZone.PROMOTION, mainViewModel)
                }
            }
        }

        navigation(
            route = RootNavigatorGraph.SETTINGS,
            startDestination = SettingsNavigatorGraph.SETTINGS,
        ) {
            composable(route = SettingsNavigatorGraph.SETTINGS) {
                SettingsScreen(navController)
            }
            composable(route = SettingsNavigatorGraph.LANGUAGE) {
                LanguageScreen(navController)
            }
        }
    }
}
