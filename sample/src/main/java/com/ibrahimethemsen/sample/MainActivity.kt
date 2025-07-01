package com.ibrahimethemsen.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ibrahimethemsen.guguk.MockControlPanel
import com.ibrahimethemsen.guguk.addMockInterceptor
import com.ibrahimethemsen.sample.ui.theme.GugukRootTheme
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val okHttpClient = OkHttpClient.Builder()
            .addMockInterceptor(
                localServerUrl = "http://localhost:8080",
                mockEndpoints = setOf("/api/users", "/api/posts")
            )
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("")
            .client(okHttpClient)
            .build()


        setContent {
            GugukRootTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MockControlPanel()
                }
            }
        }
    }
}
