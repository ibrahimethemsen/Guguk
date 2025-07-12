package com.ibrahimethemsen.guguk

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Guguk",
        state = rememberWindowState(WindowPlacement.Maximized),
    ) {
        App()
    }
    CoroutineScope(Dispatchers.IO).launch { startLocalServer() }
}