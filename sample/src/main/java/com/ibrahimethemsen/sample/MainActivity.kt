package com.ibrahimethemsen.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ibrahimethemsen.GugukViewModel
import com.ibrahimethemsen.guguk.GugukMockControlPanel
import com.ibrahimethemsen.sample.ui.theme.GugukRootTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GugukRootTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GugukApp(innerPadding)
                }
            }
        }
    }
}

@Composable
fun GugukApp(
    innerPadding: PaddingValues,
    gugukViewModel: GugukViewModel = hiltViewModel(),
) {
    val randomQuoteState by gugukViewModel.randomQuote.collectAsState()

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (randomQuoteState.author.isNotEmpty() || randomQuoteState.content.isNotEmpty()) {
            Text("Author: ${randomQuoteState.author}")
            Text("Quote: ${randomQuoteState.content}")
        } else {
            Text("Loading quote...")
        }
        Button(onClick = { gugukViewModel.requestNewQuote() }) {
            Text("Again Request")
        }
        Spacer(modifier = Modifier.height(16.dp))
        GugukMockControlPanel()
    }
}