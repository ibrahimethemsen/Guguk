package com.ibrahimethemsen.guguk

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import gugukroot.composeapp.generated.resources.Res
import gugukroot.composeapp.generated.resources.ic_json
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

val prettyJsonFormatter = Json {
    prettyPrint = true
    encodeDefaults = true
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var targetEndpoint by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        var selectedMethodString by remember { mutableStateOf("GET") }
        val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")

        var feedbackMessage by remember { mutableStateOf<String?>(null) }
        var feedbackMessageType by remember { mutableStateOf<FeedbackType?>(null) }

        var jsonInputTextValue by remember { mutableStateOf(TextFieldValue("")) }
        var jsonParseError by remember { mutableStateOf<String?>(null) }
        var errorLine by remember { mutableStateOf<Int?>(null) }
        val coroutineScope = rememberCoroutineScope()
        var debounceJob by remember { mutableStateOf<Job?>(null) }
        var isJsonValid by remember { mutableStateOf(false) }

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
                    jsonInputTextValue = TextFieldValue(content, TextRange(content.length))
                    jsonParseError = null
                    errorLine = null
                    isJsonValid = false

                    try {
                        if (content.isNotBlank()) {
                            Json.parseToJsonElement(content)
                            isJsonValid = true
                        }
                    } catch (e: SerializationException) {
                        jsonParseError = "Yüklenen dosya geçersiz JSON: ${e.localizedMessage}"
                        errorLine = extractErrorLine(e, content)
                        isJsonValid = false
                    }

                } catch (e: Exception) {
                    jsonParseError = "Dosya okuma hatası: ${e.message}"
                    errorLine = null
                    isJsonValid = false
                }
            }
        }

        val validateJson = { jsonText: String ->
            debounceJob?.cancel()
            debounceJob = coroutineScope.launch {
                delay(500L)
                try {
                    if (jsonText.isNotBlank()) {
                        Json.parseToJsonElement(jsonText)
                        jsonParseError = null
                        errorLine = null
                        isJsonValid = true
                    } else {
                        jsonParseError = null
                        errorLine = null
                        isJsonValid = false
                    }
                } catch (e: SerializationException) {
                    val extractedLine = extractErrorLine(e, jsonText)
                    jsonParseError =
                        "Geçersiz JSON: ${e.localizedMessage.lines().firstOrNull() ?: e.message}"
                    errorLine = extractedLine
                    isJsonValid = false
                } catch (e: Exception) {
                    jsonParseError = "Beklenmedik bir hata oluştu: ${e.localizedMessage}"
                    errorLine = null
                    isJsonValid = false
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .border(
                        BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .background(Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                            .clickable { expanded = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedMethodString,
                            color = when (selectedMethodString) {
                                "GET" -> Color(0xFF28A745)
                                "POST" -> Color(0xFFFFA500)
                                "PUT" -> Color(0xFF007BFF)
                                "DELETE" -> Color(0xFFDC3545)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select HTTP Method",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
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

                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = targetEndpoint,
                    onValueChange = { targetEndpoint = it.removePrefix("/") },
                    label = { Text("Endpoint giriniz ") },
                    placeholder = { Text("Örn: /users, /products/123") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true
                )
                Image(
                    modifier = Modifier.size(40.dp).clickable {
                        selectJsonFile()
                    },
                    painter = painterResource(Res.drawable.ic_json),
                    contentDescription = null
                )
                Button(
                    onClick = {
                        if (targetEndpoint.isBlank()) {
                            feedbackMessage = "Hedef endpoint boş olamaz."
                            feedbackMessageType = FeedbackType.ERROR
                            return@Button
                        }
                        if (jsonInputTextValue.text.isNotBlank() && jsonParseError != null) {
                            feedbackMessage =
                                "Yanıt body'si için girilen JSON geçersiz: $jsonParseError"
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

                        MockServerState.setResponse(
                            pathKey,
                            ktorHttpMethod,
                            jsonInputTextValue.text
                        )

                        feedbackMessage =
                            "Mock yanıt '$pathKey' için $selectedMethodString isteğine ayarlandı."
                        feedbackMessageType = FeedbackType.SUCCESS
                    },
                    enabled = targetEndpoint.isNotBlank() && (jsonInputTextValue.text.isBlank() || jsonParseError == null),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 8.dp,
                        bottomEnd = 8.dp
                    ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text("Send", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Response Json", style = MaterialTheme.typography.titleMedium)

                if (isJsonValid) {
                    Text(
                        "✓ Geçerli JSON",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            JsonTemplateSelector(
                onTemplateSelected = { template ->
                    jsonInputTextValue = TextFieldValue(template, TextRange(template.length))
                    validateJson(template)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        try {
                            if (jsonInputTextValue.text.isNotBlank()) {
                                val jsonObject = Json.parseToJsonElement(jsonInputTextValue.text)
                                val prettyJson = prettyJsonFormatter.encodeToString(jsonObject)
                                jsonInputTextValue =
                                    TextFieldValue(prettyJson, TextRange(prettyJson.length))
                                jsonParseError = null
                                errorLine = null
                                isJsonValid = true
                            }
                        } catch (e: SerializationException) {
                            jsonParseError =
                                "JSON formatlanamadı, geçerli bir JSON değil: ${e.localizedMessage}"
                            errorLine = extractErrorLine(e, jsonInputTextValue.text)
                            isJsonValid = false
                        }
                    },
                    enabled = jsonInputTextValue.text.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.FormatIndentIncrease, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Formatla")
                }

                OutlinedButton(
                    onClick = {
                        jsonInputTextValue = TextFieldValue("")
                        jsonParseError = null
                        errorLine = null
                        isJsonValid = false
                        feedbackMessage = null
                    },
                    enabled = jsonInputTextValue.text.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Temizle")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            JsonCodeEditorWithLineNumbers(
                value = jsonInputTextValue,
                onValueChange = { newValue ->
                    jsonInputTextValue = newValue
                    feedbackMessage = null
                    validateJson(newValue.text)
                },
                isError = jsonParseError != null,
                errorLine = errorLine,
                errorMessage = jsonParseError,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

enum class FeedbackType {
    SUCCESS, ERROR, INFO
}