package com.example.security_connect_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.security_connect_app.screens.ChatbotScreen
import com.example.security_connect_app.screens.ValidationScreen

class ValidationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            MaterialTheme {
                Surface {
                    NavHost(navController = navController, startDestination = "validation") {
                        composable("validation") {
                            ValidationScreen(navController = navController)
                        }
                        composable(
                            "chatbot?autoMsg={autoMsg}",
                            arguments = listOf(
                                navArgument("autoMsg") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val autoMsg = backStackEntry.arguments?.getString("autoMsg")
                            ChatbotScreen(navController = navController, autoMsg = autoMsg)
                        }
                    }
                }
            }
        }
    }
}