package vn.iostream.apps.file_locker_x.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    left: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    right: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .then(modifier)
            .padding(horizontal = 0.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            left?.let {
                Surface(
                    modifier = Modifier.size(48.dp),
                    color = Color.Transparent,
                ) {
                    it()
                }
            }

            title?.let {
                Surface(
                    modifier = Modifier.weight(1.0f), color = Color.Transparent
                ) {
                    it()
                }
            }

            right?.let {
                Surface(
                    modifier = Modifier.size(48.dp), color = Color.Transparent,
                ) {
                    it()
                }
            }
        }
    }
}