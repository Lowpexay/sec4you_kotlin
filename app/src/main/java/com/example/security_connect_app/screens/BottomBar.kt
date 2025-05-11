package com.example.security_connect_app.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.padding

@Composable
fun BottomBar(navController: NavController, current: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BottomAppBar(
            containerColor = Color.DarkGray,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            NavigationBarItem(
                selected = current == "validation",
                onClick = { if (current != "validation") navController.navigate("validation") },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Shield, contentDescription = "Validação", tint = Color.White)
                        Text("Validação", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            NavigationBarItem(
                selected = current == "chatbot",
                onClick = {
                    if (current != "chatbot") {
                        navController.navigate("chatbot?autoMsg=")
                    }
                },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ChatBubble, contentDescription = "Chatbot", tint = Color.White)
                        Text("Chatbot", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        }
        Text(
            "Desenvolvido por Gabriel Gramacho, Mikael Palmeira, Gabriel Araujo, Gustavo Teodoro e Kauã Granata • 2025",
            color = Color.LightGray,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .padding(top = 4.dp, bottom = 8.dp)
        )
    }
}