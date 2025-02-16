package co.iostream.apps.code_pocket.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun Item() {
    IconButton(
        onClick = { /* "Open nav drawer" */ }
    ) {
        Icon(Icons.Filled.Menu, contentDescription = "Localized description")
    }
}