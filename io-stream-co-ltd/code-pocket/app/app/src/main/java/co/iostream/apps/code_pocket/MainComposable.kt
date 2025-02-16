package co.iostream.apps.code_pocket

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import co.iostream.apps.code_pocket.components.core.CustomDialog
import co.iostream.apps.code_pocket.components.core.CustomDialogComposable
import co.iostream.apps.code_pocket.navigation.RootNavigator

val customDialog = CustomDialog()

@Composable
fun MainComposable() {
    val navController: NavHostController = rememberNavController()

    RootNavigator(navController)

    // Global components
    CustomDialogComposable(customDialog)
}
