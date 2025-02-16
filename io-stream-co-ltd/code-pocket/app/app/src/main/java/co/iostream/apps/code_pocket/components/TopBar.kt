package co.iostream.apps.code_pocket.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.iostream.apps.code_pocket.ui.theme.Theme

@Composable
fun HeaderBar(
    left: @Composable (() -> Unit)? = null,
    title: String?,
    right: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Theme)
//            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(54.dp), color = Color.Transparent
            ) {
                left?.let {
                    it()
                }
            }

            title?.let {
                Text(
                    text = it,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                modifier = Modifier.size(54.dp), color = Color.Transparent
            ) {
                right?.let {
                    it()
                }
            }
        }
//        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    }
}