package com.ibrahimethemsen.guguk

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Guguk",
    ) {
        App()
    }
    CoroutineScope(Dispatchers.IO).launch { startLocalServer() }
}