package com.example.security_connect_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import com.example.security_connect_app.screens.BottomBar

@Composable
fun ValidationScreen(navController: NavController) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var selectedType by remember { mutableStateOf("Email") }
    var result by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showHelpButton by remember { mutableStateOf(false) }
    var helpMessage by remember { mutableStateOf("") }
    var helpButtonText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    fun isValidEmail(email: String): Boolean {
        val regex = Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        return regex.matches(email)
    }

    fun sha1(input: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.toUpperCase()
    }

    fun checkPasswordLeak(password: String) {
        isLoading = true
        result = null
        coroutineScope.launch(Dispatchers.IO) {
            val sha1Hash = sha1(password)
            val prefix = sha1Hash.substring(0, 5)
            val suffix = sha1Hash.substring(5)
            val url = "https://api.pwnedpasswords.com/range/$prefix"
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            var found = false
            var count = ""
            for (line in body.split("\n")) {
                val parts = line.split(":")
                if (parts[0].equals(suffix, ignoreCase = true)) {
                    found = true
                    count = parts[1].trim()
                    break
                }
            }
            withContext(Dispatchers.Main) {
                isLoading = false
                if (found) {
                    result = "⚠️ Sua senha foi encontrada em vazamentos ($count vezes)."
                    showHelpButton = true
                    helpMessage = "Minha senha vazou, o que posso fazer?"
                    helpButtonText = "Sua senha foi vazada. Gostaria de  algumas dicas de como se proteger com o nosso chat? Clique Aqui!"
                } else {
                    result = "✅ Sua senha NÃO foi encontrada em vazamentos!"
                    showHelpButton = false
                }
            }
        }
    }

    fun checkEmailLeak(email: String) {
        isLoading = true
        result = null
        coroutineScope.launch {
            delay(1000) // Simulação de consulta
            isLoading = false
            if (email.endsWith("@test.com") || email.contains("leak")) {
                result = "⚠️ O email $email foi encontrado em vazamentos!"
                showHelpButton = true
                helpMessage = "Meu email vazou, o que posso fazer?"
                helpButtonText = "Seu email foi vazado. Gostaria de  algumas dicas de como se proteger com o nosso chat? Clique Aqui!"
            } else {
                result = "✅ O email $email NÃO foi encontrado em vazamentos!"
                showHelpButton = false
            }
        }
    }

    fun verifyData() {
        val data = input.text.trim()
        if (data.isEmpty()) {
            result = "❌ Digite algo para verificar."
            showHelpButton = false
            return
        }
        if (selectedType == "Email") {
            if (!isValidEmail(data)) {
                result = "❌ Formato de email inválido."
                showHelpButton = false
                return
            }
            checkEmailLeak(data)
        } else {
            if (data.length < 6) {
                result = "❌ Senha muito curta. Pelo menos 6 caracteres."
                showHelpButton = false
                return
            }
            checkPasswordLeak(data)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp, start = 24.dp, end = 24.dp, top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Validação de Dados Vazados", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Verifique se seu e-mail ou senha já apareceram em vazamentos públicos. Assim, você pode agir rapidamente para proteger suas contas.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Button(
                    onClick = {
                        selectedType = "Email"
                        input = TextFieldValue("")
                        result = null
                        showHelpButton = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == "Email") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) { Text("Email") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        selectedType = "Senha"
                        input = TextFieldValue("")
                        result = null
                        showHelpButton = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == "Senha") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) { Text("Senha") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(if (selectedType == "Email") "Digite seu e-mail" else "Digite sua senha", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { verifyData() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Validar")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            }
            result?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            }
        }

        // Botão flutuante de ajuda
        // Botão flutuante de ajuda centralizado e largo
        if (showHelpButton) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 24.dp, end = 24.dp, bottom = 180.dp) // mesmo padding lateral do restante da tela
            ) {
                Button(
                    onClick = { navController.navigate("chatbot?autoMsg=${helpMessage}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), // altura igual ao botão "Validar"
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(helpButtonText, color = Color.White)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            BottomBar(navController, current = "validation")
        }
    }
}