package com.example.security_connect_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun ChatbotScreen(navController: NavController, autoMsg: String? = null) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun sendMessage(msg: String? = null) {
        val userMsg = msg ?: input.text
        if (userMsg.isNotBlank()) {
            messages = messages + ChatMessage(userMsg, true)
            input = TextFieldValue("")
            isLoading = true
            coroutineScope.launch(Dispatchers.IO) {
                val botReply = askGemini(userMsg)
                withContext(Dispatchers.Main) {
                    messages = messages + ChatMessage(botReply, false)
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(autoMsg) {
        if (!autoMsg.isNullOrBlank()) {
            input = TextFieldValue(autoMsg)
            sendMessage(autoMsg)
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
                .padding(bottom = 88.dp, start = 24.dp, end = 24.dp, top = 24.dp)
        ) {
            Text("Chatbot", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true
            ) {
                items(messages.reversed()) { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                msg.text,
                                modifier = Modifier.padding(12.dp),
                                color = Color.White
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Digite sua mensagem", color = Color.White) },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
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
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { sendMessage() }, enabled = !isLoading) {
                    Text("Enviar")
                }
            }
            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            BottomBar(navController, current = "chatbot")
        }
    }
}

suspend fun askGemini(userMsg: String): String {
    val apiKey = "api_key"
    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
    val client = OkHttpClient.Builder()
    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
    .build()
    val json = JSONObject()

    // Prompt fixo
    val prompt = """
        Você é Luiz, assistente virtual da Sec4You, especializado apenas em temas de **segurança da informação**. Responda **em português brasileiro**.

        📌 **Instruções gerais:**
        - Seja objetivo e amigável, mas direto.
        - Não inicie toda mensagem com saudações como "Olá", "Oi", "Tudo bem?". Apenas a interação inicial.
        - Responda usando frases curtas e simples.
        - Não escreva mais do que o necessário para ser claro.

        🎭 **Tom emocional:**
        - Analise a mensagem do usuário e indique o tom no formato [TOM: feliz, bravo, triste, explicando, neutro] antes da resposta.

        🚫 **Assuntos fora do contexto:**
        - Se o tema não for relacionado à **segurança da informação**, responda apenas:
          "Desculpe, não posso te ajudar com isso. Sobre o que de segurança você gostaria de saber?"

        📩 **Mensagem do usuário:**  
        $userMsg
    """.trimIndent()

    val partsArray = org.json.JSONArray()
    partsArray.put(JSONObject().put("text", prompt))
    val contentsArray = org.json.JSONArray()
    contentsArray.put(JSONObject().put("parts", partsArray))
    json.put("contents", contentsArray)

    val body = json.toString().toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(url)
        .post(body)
        .build()
    println("Enviando para Gemini: $prompt")
    return try {
        val response = client.newCall(request).execute()
        val respBody = response.body?.string() ?: ""
        println("Resposta bruta Gemini: $respBody")
        val respJson = JSONObject(respBody)
        if (respJson.has("error")) {
            return respJson.getJSONObject("error").optString("message", "Erro desconhecido da API Gemini.")
        }
        val contentArr = respJson.optJSONArray("candidates")
        if (contentArr != null && contentArr.length() > 0) {
            val content = contentArr.getJSONObject(0)
            val msg = content.getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
            msg
        } else {
            "Desculpe, não consegui entender. Tente novamente."
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Erro ao conectar ao assistente: ${e.localizedMessage}"
    }
}
