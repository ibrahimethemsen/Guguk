package com.ibrahimethemsen.guguk

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var targetEndpoint by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        var selectedMethodString by remember { mutableStateOf("GET") }
        val methods = listOf("GET", "POST", "PUT", "DELETE")

        var feedbackMessage by remember { mutableStateOf<String?>(null) }
        var feedbackMessageType by remember { mutableStateOf<FeedbackType?>(null) }

        var jsonInputText by remember { mutableStateOf("") }
        var jsonParseError by remember { mutableStateOf<String?>(null) }
        var errorLine by remember { mutableStateOf<Int?>(null) }
        val coroutineScope = rememberCoroutineScope()
        var debounceJob by remember { mutableStateOf<Job?>(null) }


        val selectJsonFile: () -> Unit = {
            val fileDialog =
                FileDialog(null as Frame?, "JSON Dosyası Seç (Yanıt Body)", FileDialog.LOAD)
            fileDialog.setFilenameFilter { _, name -> name.lowercase().endsWith(".json") }
            fileDialog.isVisible = true

            val selectedFile = fileDialog.file
            val selectedDirectory = fileDialog.directory

            if (selectedFile != null && selectedDirectory != null) {
                val file = File(selectedDirectory, selectedFile)
                try {
                    val content = file.readText(Charsets.UTF_8)
                    jsonInputText = content
                    jsonParseError =
                        null
                    errorLine = null
                    try {
                        if (content.isNotBlank()) Json.parseToJsonElement(content)
                    } catch (e: SerializationException) {
                        jsonParseError = "Yüklenen dosya geçersiz JSON: ${e.localizedMessage}"
                        errorLine = extractErrorLine(e, content)
                    }

                } catch (e: Exception) {
                    jsonParseError = "Dosya okuma hatası: ${e.message}"
                    errorLine = null
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Mock Yanıt Tanımlama Ekranı", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = targetEndpoint,
                onValueChange = { targetEndpoint = it.removePrefix("/") },
                label = { Text("Hedef Endpoint (örn: users)") },
                placeholder = { Text("Örn: /users, /products/123") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bu Endpoint'e Gelen", modifier = Modifier.padding(end = 8.dp))
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(selectedMethodString)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        methods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    selectedMethodString = method
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Text(" İsteğine Verilecek Yanıt:", modifier = Modifier.padding(start = 8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))


            Text("Yanıt Body'si (JSON):", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            JsonCodeEditorWithLineNumbers(
                value = jsonInputText,
                onValueChange = { newText ->
                    jsonInputText = newText
                    feedbackMessage = null
                    debounceJob?.cancel()
                    debounceJob = coroutineScope.launch {
                        delay(300)
                        try {
                            if (newText.isNotBlank()) {
                                Json.parseToJsonElement(newText)
                                jsonParseError = null
                                errorLine = null
                            } else {
                                jsonParseError = null
                                errorLine = null
                            }
                        } catch (e: SerializationException) {
                            jsonParseError = "Geçersiz JSON formatı: ${e.localizedMessage}"
                            errorLine = extractErrorLine(e, newText)
                        } catch (e: Exception) {
                            jsonParseError = "Bir hata oluştu: ${e.localizedMessage}"
                            errorLine = null
                        }
                    }
                },
                isError = jsonParseError != null,
                errorLine = errorLine,
                errorMessage = jsonParseError,
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = selectJsonFile) {
                Text("Yanıt JSON'ını Dosyadan Yükle")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (targetEndpoint.isBlank()) {
                        feedbackMessage = "Hedef endpoint boş olamaz."
                        feedbackMessageType = FeedbackType.ERROR
                        return@Button
                    }
                    if (jsonInputText.isNotBlank() && jsonParseError != null) {
                        feedbackMessage = "Yanıt body'si için girilen JSON geçersiz: $jsonParseError"
                        feedbackMessageType = FeedbackType.ERROR
                        return@Button
                    }

                    val pathKey = "/${targetEndpoint.trimStart('/')}"
                    val ktorHttpMethod = when (selectedMethodString) {
                        "GET" -> HttpMethod.Get
                        "POST" -> HttpMethod.Post
                        "PUT" -> HttpMethod.Put
                        "DELETE" -> HttpMethod.Delete
                        else -> {
                            feedbackMessage = "Geçersiz HTTP metodu seçildi."
                            feedbackMessageType = FeedbackType.ERROR
                            return@Button
                        }
                    }

                    MockServerState.setResponse(pathKey, ktorHttpMethod, jsonInputText)

                    feedbackMessage = "Mock yanıt '$pathKey' için $selectedMethodString isteğine ayarlandı."
                    feedbackMessageType = FeedbackType.SUCCESS
                },
                modifier = Modifier.align(Alignment.End),
                enabled = targetEndpoint.isNotBlank() && (jsonInputText.isBlank() || jsonParseError == null)
            ) {
                Text("Mock Yanıtı Ayarla/Güncelle")
            }

            if (feedbackMessage != null && feedbackMessageType != null) {
                Spacer(modifier = Modifier.height(16.dp))
                val textColor = when (feedbackMessageType) {
                    FeedbackType.SUCCESS -> Color(0xFF4CAF50)
                    FeedbackType.ERROR -> MaterialTheme.colorScheme.error
                    FeedbackType.INFO -> MaterialTheme.colorScheme.onSurface
                    null -> MaterialTheme.colorScheme.onSurface
                }
                Text(
                    feedbackMessage!!,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().border(1.dp, textColor.copy(alpha = 0.5f))
                        .padding(8.dp)
                )
            }
        }
    }
}

enum class FeedbackType {
    SUCCESS, ERROR, INFO
}
