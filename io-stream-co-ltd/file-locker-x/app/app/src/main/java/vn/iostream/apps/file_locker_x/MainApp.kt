package vn.iostream.apps.file_locker_x

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import vn.iostream.apps.file_locker_x.ui.composables.core.CustomDialog
import vn.iostream.apps.file_locker_x.ui.composables.core.CustomDialogComposable
import vn.iostream.apps.file_locker_x.navigation.RootNavigator

val customDialog: CustomDialog = CustomDialog()

@Composable
fun MainApp(navController: NavHostController = rememberNavController()) {

    RootNavigator(navController)

    // Global components
    CustomDialogComposable(customDialog)
}
