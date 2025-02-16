package vn.iostream.apps.file_locker_x.ui.composables

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.iostream.apps.file_locker_x.customDialog

data class DialogItem(
    val context: Context,
    val titleResIdInt: Int,
    val subtitleResIdInt: Int,
    val coroutineScope: CoroutineScope,
    val onConfirmCallback:
    suspend () -> Unit = {}
){
    val onClick = {
        if (!customDialog.getState()) {
            customDialog.setDialogVisible(false)
            customDialog.confirm()
            customDialog.onConfirmCallback = {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        onConfirmCallback()
                    }
                }
            }
            customDialog.setDialogVisible(true)
            customDialog.title =
                context.getString(titleResIdInt)
            customDialog.subTitle =
                context.getString(subtitleResIdInt)
        }

        customDialog.enable(!customDialog.getState())
    }
}

data class MenuItem(
    var onClick: () -> Unit = {},
    var iconResId: Int,
    var title: String
) {
    constructor(dialogItem: DialogItem, iconResId: Int, title: String) : this(
        dialogItem.onClick, iconResId, title
    )

}

@Composable
fun DropdownMenuItem(
    item: MenuItem
){
    DropdownMenuItem(onClick = item.onClick, text = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = item.iconResId),
                contentDescription = null,
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = item.title
            )
        }
    })
}