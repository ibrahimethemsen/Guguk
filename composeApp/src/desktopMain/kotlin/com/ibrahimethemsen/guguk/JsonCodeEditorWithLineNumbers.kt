package com.ibrahimethemsen.guguk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.SerializationException

@Composable
fun JsonCodeEditorWithLineNumbers(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    errorLine: Int? = null,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val lines = value.split("\n")
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .border(
                1.dp,
                if (isError) Color.Red else Color.Gray,
                shape = MaterialTheme.shapes.small
            )
            .background(Color(0xFF23272E))
            .padding(0.dp)
            .height(320.dp)
    ) {
        Row(Modifier.fillMaxWidth().weight(2f)) {
            Column(
                Modifier.fillMaxHeight()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (i in lines.indices) {
                    val color = if (isError && errorLine == i + 1) Color.Red else Color.Gray
                    Text(
                        (i + 1).toString(),
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp
                    )
                }
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(vertical = 8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    color = Color(0xFFD8DEE9)
                ),
                cursorBrush = SolidColor(Color(0xFF61AFEF)),
                decorationBox = { innerTextField ->
                    Box(Modifier.fillMaxSize().padding(end = 8.dp)) {
                        if (value.isEmpty()) {
                            Text(
                                "{\n  \"key\": \"value\"\n}",
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        innerTextField()
                    }
                },
                visualTransformation = {
                    val highlighted = jsonSyntaxHighlightPreserveIndent(it.text)
                    TransformedText(highlighted, OffsetMapping.Identity)
                },
                maxLines = Int.MAX_VALUE,
                singleLine = false
            )
        }
        if (isError && errorMessage != null) {
            Column(
                Modifier.padding(top = 8.dp, start = 8.dp, bottom = 8.dp).fillMaxWidth().weight(1f)
            ) {
                Text(
                    text = if (errorLine != null) "Hata (Satır $errorLine): $errorMessage" else "Hata: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

fun jsonSyntaxHighlightPreserveIndent(json: String): AnnotatedString {
    val keyColor = Color(0xFF61AFEF)
    val stringColor = Color(0xFF98C379)
    val numberColor = Color(0xFFD19A66)
    val booleanColor = Color(0xFFC678DD)
    val nullColor = Color(0xFFE06C75)
    val bracketColor = Color(0xFF56B6C2)
    val commaColor = Color(0xFFABB2BF)
    val defaultColor = Color(0xFFD8DEE9)
    val builder = AnnotatedString.Builder()
    var i = 0
    while (i < json.length) {
        try {
            when (val c = json[i]) {
                ' ', '\t' -> {
                    val start = i
                    while (i < json.length && (json[i] == ' ' || json[i] == '\t')) i++
                    builder.withStyle(SpanStyle(color = defaultColor)) {
                        append(
                            json.substring(
                                start,
                                i
                            )
                        )
                    }
                }

                '\n' -> {
                    builder.append("\n")
                    i++
                }

                '{', '}', '[', ']' -> {
                    builder.withStyle(
                        SpanStyle(
                            color = bracketColor,
                            fontWeight = FontWeight.Bold
                        )
                    ) { append(c) }
                    i++
                }

                ':', ',' -> {
                    builder.withStyle(SpanStyle(color = commaColor)) { append(c) }
                    i++
                }

                '"' -> {
                    val start = i
                    i++
                    var closed = false
                    while (i < json.length) {
                        if (json[i] == '\\' && i + 1 < json.length) i++ // escape
                        else if (json[i] == '"') {
                            closed = true; break
                        }
                        i++
                    }
                    if (closed) {
                        i++
                        val str = json.substring(start, i)
                        val isKey = i < json.length && json.drop(i).trimStart().startsWith(":")
                        builder.withStyle(SpanStyle(color = if (isKey) keyColor else stringColor)) {
                            append(
                                str
                            )
                        }
                    } else {
                        builder.withStyle(SpanStyle(color = stringColor)) {
                            append(
                                json.substring(
                                    start
                                )
                            )
                        }
                        break
                    }
                }

                in '0'..'9', '-' -> {
                    val start = i
                    while (i < json.length && (json[i].isDigit() || json[i] == '.' || json[i] == '-')) i++
                    builder.withStyle(SpanStyle(color = numberColor)) {
                        append(
                            json.substring(
                                start,
                                i
                            )
                        )
                    }
                }

                't', 'f' -> {
                    if (json.startsWith("true", i)) {
                        builder.withStyle(SpanStyle(color = booleanColor)) { append("true") }
                        i += 4
                    } else if (json.startsWith("false", i)) {
                        builder.withStyle(SpanStyle(color = booleanColor)) { append("false") }
                        i += 5
                    } else {
                        builder.append(c)
                        i++
                    }
                }

                'n' -> {
                    if (json.startsWith("null", i)) {
                        builder.withStyle(SpanStyle(color = nullColor)) { append("null") }
                        i += 4
                    } else {
                        builder.append(c)
                        i++
                    }
                }

                else -> {
                    builder.withStyle(SpanStyle(color = defaultColor)) { append(c) }
                    i++
                }
            }
        } catch (e: Exception) {
            builder.withStyle(SpanStyle(color = defaultColor)) { append(json.substring(i)) }
            break
        }
    }
    return builder.toAnnotatedString()
}


fun extractErrorLine(exception: SerializationException, jsonString: String): Int? {
    val message = exception.localizedMessage ?: return null
    val linePattern = "line (\\d+)".toRegex()
    val lineMatch = linePattern.find(message)
    if (lineMatch != null) {
        return lineMatch.groupValues[1].toIntOrNull()
    }

    val offsetPattern = "offset (\\d+)".toRegex()
    val offsetMatch = offsetPattern.find(message)
    if (offsetMatch != null) {
        val offset = offsetMatch.groupValues[1].toIntOrNull()
        if (offset != null) {
            var currentLine = 1
            for (i in 0 until offset) {
                if (i < jsonString.length && jsonString[i] == '\n') {
                    currentLine++
                }
            }
            return currentLine
        }
    }

    return null
}