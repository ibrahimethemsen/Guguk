package com.ibrahimethemsen.guguk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.SerializationException
import kotlin.math.roundToInt

@Composable
fun JsonCodeEditorWithLineNumbers(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    errorLine: Int?,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    val editorTextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        color = Color(0xFFE4E6EA),
        lineHeight = 20.sp
    )

    val lines = remember(value.text) { value.text.split('\n') }
    val density = LocalDensity.current
    val editorLineHeightDp = with(density) { editorTextStyle.lineHeight.toDp() }
    val verticalPadding = 12.dp

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                2.dp,
                if (isError) MaterialTheme.colorScheme.error else Color(0xFF3E4451),
                shape = RoundedCornerShape(8.dp)
            )
            .background(Color(0xFF1E1E1E))
            .padding(0.dp)
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color(0xFF252526))
                .padding(vertical = verticalPadding)
                .padding(horizontal = 12.dp)
        ) {
            lines.forEachIndexed { index, _ ->
                val lineNumber = index + 1
                val currentLineIsError = isError && errorLine == lineNumber
                Text(
                    text = lineNumber.toString().padStart(lines.size.toString().length),
                    style = editorTextStyle.copy(
                        color = if (currentLineIsError) Color(0xFFFF6B6B) else Color(0xFF858585),
                        textAlign = TextAlign.End,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier
                        .height(editorLineHeightDp)
                        .then(
                            if (currentLineIsError) Modifier.background(
                                Color(0xFFFF6B6B).copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            ) else Modifier
                        )
                        .padding(end = 8.dp)
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    val enhancedValue = enhanceJsonEditing(value, newValue)
                    onValueChange(enhancedValue)
                },
                textStyle = editorTextStyle,
                cursorBrush = SolidColor(Color(0xFF61AFEF)),
                visualTransformation = { textFieldValue ->
                    val annotatedString = modernJsonSyntaxHighlighting(
                        json = textFieldValue.text,
                        isError = isError,
                        errorLineNum = errorLine,
                    )
                    TransformedText(annotatedString, OffsetMapping.Identity)
                },
                decorationBox = { innerTextField ->
                    Row(Modifier.fillMaxSize()) {
                        Box(
                            Modifier
                                .padding(horizontal = 12.dp)
                                .padding(vertical = verticalPadding)
                        ) {
                            if (value.text.isEmpty()) {
                                Text(
                                    "{\n  \"sehir\": \"Nevsehir\",\n  \"plaka\": 50,\n  \"yerli\": true\n}",
                                    style = editorTextStyle.copy(color = Color(0xFF6A737D))
                                )
                            }
                            innerTextField()
                        }
                    }
                },
                maxLines = Int.MAX_VALUE,
                singleLine = false,
                modifier = Modifier.fillMaxSize()
            )


            if (isError && errorLine != null && errorMessage != null) {
                val singleLineHeightPx = with(density) {
                    editorTextStyle.lineHeight.toPx()
                }
                val errorLineContentStartYPx =
                    (errorLine - 1) * singleLineHeightPx + with(density) { verticalPadding.toPx() }
                val errorMessageYOffsetPx =
                    errorLineContentStartYPx + singleLineHeightPx + with(density) { 8.dp.toPx() }

                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6B6B),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = 12.dp.roundToPx(),
                                y = errorMessageYOffsetPx.roundToInt()
                            )
                        }
                        .fillMaxWidth()
                        .padding(end = 12.dp)
                        .background(
                            Color(0xFFFF6B6B).copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .pointerInput(Unit) {}
                )
            }
        }
    }
}

private fun enhanceJsonEditing(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
    var enhancedText = newValue.text
    var enhancedSelection = newValue.selection

    val lastChar = if (oldValue.text.length < enhancedText.length) {
        enhancedText.getOrNull(newValue.selection.start - 1)
    } else null

    when (lastChar) {
        '{' -> {
            if (newValue.selection.start < enhancedText.length && enhancedText[newValue.selection.start] != '}') {
                enhancedText = enhancedText.substring(
                    0,
                    newValue.selection.start
                ) + "}" + enhancedText.substring(newValue.selection.start)
            } else if (newValue.selection.start == enhancedText.length) {
                enhancedText += "}"
            }
        }

        '[' -> {
            if (newValue.selection.start < enhancedText.length && enhancedText[newValue.selection.start] != ']') {
                enhancedText = enhancedText.substring(
                    0,
                    newValue.selection.start
                ) + "]" + enhancedText.substring(newValue.selection.start)
            } else if (newValue.selection.start == enhancedText.length) {
                enhancedText += "]"
            }
        }

        '"' -> {
            val charBefore = enhancedText.getOrNull(newValue.selection.start - 2)
            if (charBefore != '\\' && (newValue.selection.start == enhancedText.length || enhancedText[newValue.selection.start] != '"')) {
                if (newValue.selection.start < enhancedText.length && enhancedText[newValue.selection.start] != '"') {
                    enhancedText = enhancedText.substring(
                        0,
                        newValue.selection.start
                    ) + "\"" + enhancedText.substring(newValue.selection.start)
                } else if (newValue.selection.start == enhancedText.length) {
                    enhancedText += "\""
                }
            }
        }
    }

    if (enhancedText.length > oldValue.text.length && enhancedText.endsWith("\n") && oldValue.selection.end == oldValue.text.length) {
        val currentLineIndex =
            oldValue.text.substring(0, oldValue.selection.start).count { it == '\n' }
        val lines = oldValue.text.split('\n')
        if (currentLineIndex < lines.size) {
            val previousLine = lines.getOrElse(currentLineIndex) { "" }
            val currentIndent = previousLine.takeWhile { it.isWhitespace() }
            var newIndent = currentIndent

            if (previousLine.trimEnd().endsWith('{') || previousLine.trimEnd().endsWith('[')) {
                newIndent += "  "
            }
            enhancedText += newIndent
            enhancedSelection = TextRange(enhancedText.length)
        }
    }

    if (lastChar == '\n' && newValue.selection.start > 0) {
        val charBeforeCursor = enhancedText.getOrNull(newValue.selection.start - 2)
        val charAfterCursor = enhancedText.getOrNull(newValue.selection.start)

        if ((charBeforeCursor == '{' && charAfterCursor == '}') || (charBeforeCursor == '[' && charAfterCursor == ']')) {
            val currentLineIndex =
                enhancedText.substring(0, newValue.selection.start - 1).count { it == '\n' }
            val lines = enhancedText.split('\n')
            val indentOfParent =
                lines.getOrElse(currentLineIndex - 1) { "" }.takeWhile { it.isWhitespace() }
            val childIndent = "$indentOfParent  "

            val textBefore = enhancedText.substring(0, newValue.selection.start)
            val textAfter = enhancedText.substring(newValue.selection.start)

            enhancedText = textBefore + childIndent + "\n" + indentOfParent + textAfter.trimStart()
            enhancedSelection = TextRange(textBefore.length + childIndent.length)
        }
    }

    return TextFieldValue(enhancedText, enhancedSelection)
}

fun modernJsonSyntaxHighlighting(
    json: String,
    isError: Boolean,
    errorLineNum: Int?
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    builder.append(json)

    val keywordStyle = SpanStyle(color = Color(0xFFC586C0), fontWeight = FontWeight.Bold)
    val stringStyle = SpanStyle(color = Color(0xFFCE9178))
    val numberStyle = SpanStyle(color = Color(0xFFB5CEA8))
    val keyStyle = SpanStyle(color = Color(0xFF9CDCFE), fontWeight = FontWeight.Medium)
    val bracketStyle = SpanStyle(color = Color(0xFFDCDCAA), fontWeight = FontWeight.Bold)
    val punctuationStyle = SpanStyle(color = Color(0xFFD4D4D4))

    val keywordRegex = "\\b(true|false|null)\\b".toRegex()
    val stringRegex = "\"[^\"]*\"".toRegex()
    val numberRegex = "\\b-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b".toRegex()
    val keyRegex = "\"[^\"]*\"(?=\\s*:)".toRegex()
    val bracketRegex = "[{}\\[\\]]".toRegex()
    val punctuationRegex = "[:,]".toRegex()

    numberRegex.findAll(json).forEach { matchResult ->
        builder.addStyle(numberStyle, matchResult.range.first, matchResult.range.last + 1)
    }

    keywordRegex.findAll(json).forEach { matchResult ->
        builder.addStyle(keywordStyle, matchResult.range.first, matchResult.range.last + 1)
    }

    stringRegex.findAll(json).forEach { matchResult ->
        builder.addStyle(stringStyle, matchResult.range.first, matchResult.range.last + 1)
    }

    keyRegex.findAll(json).forEach { matchResult ->
        builder.addStyle(keyStyle, matchResult.range.first, matchResult.range.last + 1)
    }

    bracketRegex.findAll(json).forEach { matchResult ->
        builder.addStyle(bracketStyle, matchResult.range.first, matchResult.range.last + 1)
    }

    punctuationRegex.findAll(json).forEach { matchResult ->
        builder.addStyle(punctuationStyle, matchResult.range.first, matchResult.range.last + 1)
    }

    if (isError && errorLineNum != null && errorLineNum > 0) {
        val lines = json.split('\n')
        if (errorLineNum <= lines.size) {
            val errorLineIndex = errorLineNum - 1
            var startCharIndex = 0
            for (i in 0 until errorLineIndex) {
                startCharIndex += lines[i].length + 1
            }
            val endCharIndex = startCharIndex + lines[errorLineIndex].length

            if (startCharIndex < endCharIndex && endCharIndex <= json.length) {
                builder.addStyle(
                    SpanStyle(
                        background = Color(0xFFFF6B6B).copy(alpha = 0.3f),
                        color = Color(0xFFFF6B6B)
                    ),
                    startCharIndex,
                    endCharIndex
                )
            }
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