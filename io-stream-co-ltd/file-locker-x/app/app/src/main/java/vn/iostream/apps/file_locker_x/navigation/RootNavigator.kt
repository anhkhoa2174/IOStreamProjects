package vn.iostream.apps.file_locker_x.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import vn.iostream.apps.file_locker_x.models.FileItem
import vn.iostream.apps.file_locker_x.ui.screens.BatchScreen
import vn.iostream.apps.file_locker_x.ui.screens.ImageViewerScreen
import vn.iostream.apps.file_locker_x.ui.screens.FileManagerScreen
import vn.iostream.apps.file_locker_x.ui.screens.settings.WebviewScreen
import vn.iostream.apps.file_locker_x.ui.screens.settings.LanguageScreen
import vn.iostream.apps.file_locker_x.ui.screens.settings.PromotionScreen
import vn.iostream.apps.file_locker_x.ui.screens.settings.SettingsScreen
import vn.iostream.apps.file_locker_x.viewmodels.BatchViewModel
import vn.iostream.apps.file_locker_x.viewmodels.FileManagerViewModel

object SettingsNavigatorGraph {
    const val SETTINGS = "settings_settings"
    const val LANGUAGES = "settings_language"
    const val PROMOTION = "settings_promotion"
}

object ProcessingNavigatorGraph {
    const val SINGLE = "processing_single"
    const val BATCH = "processing_batch"
}

object RootNavigatorGraph {
    const val PROCESSING = "root_processing"
    const val SETTINGS = "root_settings"
}

object MiscNavigatorGraph {
    const val WEBVIEW = "misc_webview"
    const val MEDIAVIEW = "misc_mediaview"
}

@Composable
fun RootNavigator(
    navController: NavHostController
) {
    val fileManagerViewModel: FileManagerViewModel = hiltViewModel()
    val batchViewModel: BatchViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = RootNavigatorGraph.PROCESSING) {
        navigation(
            route = RootNavigatorGraph.PROCESSING,
            startDestination = ProcessingNavigatorGraph.SINGLE
        ) {
            composable(route = ProcessingNavigatorGraph.SINGLE) {
                FileManagerScreen(navController, fileManagerViewModel)
            }
            composable(
                route = "${ProcessingNavigatorGraph.BATCH}/{type}", arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString(
                    "type", FileItem.FeatureType.Encode.name
                ) ?: FileItem.FeatureType.Encode.name

                BatchScreen(
                    navController,
                    enumValueOf(type),
                    fileManagerViewModel,
                    batchViewModel
                )
            }
        }

        navigation(
            route = RootNavigatorGraph.SETTINGS, startDestination = SettingsNavigatorGraph.SETTINGS
        ) {
            composable(route = SettingsNavigatorGraph.SETTINGS) {
                SettingsScreen(navController, fileManagerViewModel)
            }
            composable(route = SettingsNavigatorGraph.PROMOTION) {
                PromotionScreen(navController)
            }
            composable(route = SettingsNavigatorGraph.LANGUAGES) {
                LanguageScreen(navController)
            }
        }

        composable(
            route = "${MiscNavigatorGraph.MEDIAVIEW}/{uri}", arguments = listOf(
                navArgument("uri") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val uri = backStackEntry.arguments?.getString(
                "uri"
            ) ?: ""

            ImageViewerScreen(
                imageUrl = uri,
                onDismiss = {},
                onImageClick = {},
            )
        }
        composable(
            route = "${MiscNavigatorGraph.WEBVIEW}/{uri}", arguments = listOf(
                navArgument("uri") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val uri = backStackEntry.arguments?.getString(
                "uri"
            ) ?: ""

            WebviewScreen(uri)
        }
    }
}