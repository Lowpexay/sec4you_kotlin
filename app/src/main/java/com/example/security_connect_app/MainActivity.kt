package com.example.security_connect_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.security_connect_app.navigation.AppNavigation
import com.example.security_connect_app.ui.theme.SecurityConnectAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecurityConnectAppTheme {
                val navController = rememberNavController()
                var isLoggedIn by rememberSaveable { mutableStateOf(false) }

                AppNavigation(navController, isLoggedIn) { isLoggedIn = true }
            }
        }
    }
}
