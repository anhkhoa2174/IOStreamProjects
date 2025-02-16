package com.example.deeplink

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.deeplink.ui.theme.DeepLinkTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeepLinkTheme {
                val navController = rememberNavController()
                var facebookUsername by remember { mutableStateOf("") }
                var Appname by remember { mutableStateOf("") }
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                OpenGoogle()

                                OpenYoutube()

                                OpenFacebook()

                                OutlinedTextField(
                                    value = facebookUsername,
                                    onValueChange= { facebookUsername = it },
                                    label = { Text("Nhập tên người bạn muốn stalk") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OpenPersonalFacebook(facebookUsername)

                                OutlinedTextField(
                                    value = Appname,
                                    onValueChange= { Appname = it },
                                    label = { Text("Nhập App muốn mở") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OpenApp(Appname)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun OpenGoogle() {
    val context = LocalContext.current
    Button(onClick = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
        context.startActivity(intent)
    }) {
        Text("Mở Google")
    }
}

@Composable
fun OpenYoutube(){
    val context = LocalContext.current
    Button(onClick = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
        context.startActivity(intent)
    }) {
        Text("Mở YouTube")
    }
}

@Composable
fun OpenFacebook() {
    val context = LocalContext.current
    Button(onClick = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com"))
        context.startActivity(intent)
    }) {
        Text("Mở Facebook")
    }
}

@Composable
fun OpenPersonalFacebook(username: String) {
    val context = LocalContext.current
    Button(onClick = {
        val deepLink = "https://www.facebook.com/$username"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val webUrl = "https://www.facebook.com/$username"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            context.startActivity(webIntent)
        }
    }) {Text("Mở trang cá nhân")
    }
}

@Composable
fun OpenApp(name: String) {
    val context = LocalContext.current
    Button(onClick = {
        val deepLink = "https://www.$name.com"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val webUrl = "https://www.$name.com"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            context.startActivity(webIntent)
        }
    }) {Text("Mở App")
    }
}