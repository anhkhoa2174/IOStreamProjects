package co.iostream.apps.code_pocket.ui.utils

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MenuButton(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    @DrawableRes id: Int,
    onClick: () -> Unit = {}
) {
    val iconColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.background
    }
    Surface(
        color = backgroundColor,
        shape = CircleShape,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.size(50.dp, 50.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable( onClick = { onClick() } )
                .size(40.dp, 40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(painter = painterResource(id), contentDescription = null, tint= iconColor)
        }
    }
}