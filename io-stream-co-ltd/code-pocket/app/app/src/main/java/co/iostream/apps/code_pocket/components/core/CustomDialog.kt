package co.iostream.apps.code_pocket.components.core

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import co.iostream.apps.code_pocket.R

interface ICustomDialog {
    fun getState(): Boolean
    fun enable(value: Boolean)
    fun confirm()
    fun cancel()
    fun dismiss()
}

class CustomDialog : ICustomDialog {
    private var _visible = MutableStateFlow(false)
    val visible = _visible.asStateFlow()

    var title: String = ""
    var subTitle: String = ""
    var onConfirmCallback: () -> Unit = {}
    var onCancelCallback: () -> Unit = {}
    var onDismissCallback: () -> Unit = {}

    override fun getState(): Boolean = visible.value

    override fun enable(value: Boolean) {
        _visible.value = value
    }

    override fun confirm() {
        onConfirmCallback()
        _visible.value = false
    }

    override fun cancel() {
        onCancelCallback()
        _visible.value = false
    }

    override fun dismiss() {
        onDismissCallback()
        _visible.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDialogComposable(dialogControl: CustomDialog) {
    val visible by dialogControl.visible.collectAsState()

    if (visible) {
        AlertDialog(modifier = Modifier.fillMaxWidth(0.84f), properties = DialogProperties(
            usePlatformDefaultWidth = false
        ), onDismissRequest = { dialogControl.dismiss() }) {
            Surface(
                modifier = Modifier.wrapContentSize(),
                shape = MaterialTheme.shapes.small,
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = dialogControl.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        fontSize = 14.sp,
                        text = dialogControl.subTitle,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { dialogControl.cancel() },
                        ) {
                            Text(stringResource(id = R.string.cancel))
                        }
                        TextButton(
                            onClick = { dialogControl.confirm() },
                        ) {
                            Text(stringResource(id = R.string.okay))
                        }
                    }
                }
            }
        }
    }
}