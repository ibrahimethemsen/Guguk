package com.ibrahimethemsen.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.ibrahimethemsen.guguk.GugukMockControlPanel
import com.ibrahimethemsen.sample.presentation.detail.DetailScreen
import com.ibrahimethemsen.sample.presentation.home.HomeScreen
import com.ibrahimethemsen.sample.ui.theme.GugukRootTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen {
    data object Home : Screen()
    data class Detail(val quoteId: String) : Screen()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GugukRootTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        GugukApp(innerPadding = innerPadding)

                        FloatingActionButtonMenu(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            GugukMockControlPanel()
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun GugukApp(
    innerPadding: PaddingValues,
) {
    val backStack = remember { mutableStateListOf<Any>(Screen.Home) }

    NavDisplay(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = { route ->
            when (route) {
                is Screen.Home -> NavEntry(route) {
                    HomeScreen(
                        onNavigateToDetail = { quoteId ->
                            backStack.add(Screen.Detail(quoteId))
                        }
                    )
                }

                is Screen.Detail -> NavEntry(route) {
                    DetailScreen(quoteId = route.quoteId) {
                        backStack.removeLastOrNull()
                    }
                }

                else -> NavEntry(Unit) { Text("Unknown route: $route") }
            }
        }
    )
}
