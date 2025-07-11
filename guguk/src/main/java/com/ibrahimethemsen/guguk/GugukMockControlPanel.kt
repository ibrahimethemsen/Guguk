package com.ibrahimethemsen.guguk
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun rememberMockDataManager(): GugukMockDataManager {
    return remember { GugukMockDataManager.getInstance() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GugukMockControlPanel(
    mockManager: GugukMockDataManager = rememberMockDataManager()
) {
    var newEndpoint by remember { mutableStateOf("") }
    var mockEndpoints by remember { mutableStateOf(mockManager.getMockEndpoints()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Guguk Control Panel",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newEndpoint,
                    onValueChange = { newEndpoint = it },
                    label = { Text("Endpoint") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("/api/users") }
                )
                Button(
                    onClick = {
                        if (newEndpoint.isNotBlank()) {
                            mockManager.addMockEndpoint(newEndpoint)
                            mockEndpoints = mockManager.getMockEndpoints()
                            newEndpoint = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mock Endpoints:",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                modifier = Modifier.height(200.dp)
            ) {
                items(mockEndpoints.toList()) { endpoint ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = endpoint,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                mockManager.removeMockEndpoint(endpoint)
                                mockEndpoints = mockManager.getMockEndpoints()
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    mockManager.clearMockEndpoints()
                    mockEndpoints = mockManager.getMockEndpoints()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear All Endpoints")
            }
        }
    }
}

@Preview
@Composable
private fun PrevMockControlPanel(){
    GugukMockControlPanel()
}