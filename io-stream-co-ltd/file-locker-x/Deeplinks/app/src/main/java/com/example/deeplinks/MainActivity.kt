package com.example.deeplinks

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.deeplinks.ui.theme.DeeplinksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeeplinksTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        var appName by remember { mutableStateOf("") }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            OutlinedTextField(
                                value = appName,
                                onValueChange = { appName = it },
                                label = { Text("App Name") }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                openApp(appName)
                            }) {
                                Text(text = "Open App")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openApp(appName: String) {
        val intent = packageManager.getLaunchIntentForPackage(appName)
        if (intent != null) {
            startActivity(intent)
        } else {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://$appName")
                }
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
