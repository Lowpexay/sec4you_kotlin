package com.example.security_connect_app.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(rootNavController: NavController) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "chatbot") {
        composable("chatbot") {
            ChatbotScreen(navController)
        }
        composable("validation") {
            ValidationScreen(navController)
        }
    }
}
