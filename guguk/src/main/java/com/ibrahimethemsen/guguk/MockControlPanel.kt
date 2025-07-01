package com.ibrahimethemsen.guguk
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun rememberMockDataManager(): MockDataManager {
    return remember { MockDataManager.getInstance() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockControlPanel(
    mockManager: MockDataManager = rememberMockDataManager()
) {
    var newEndpoint by remember { mutableStateOf("") }
    var mockEndpoints by remember { mutableStateOf(mockManager.getMockEndpoints()) }
    var isEnabled by remember { mutableStateOf(mockManager.isEnabled()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Mock Data Control Panel",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Enable/Disable Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mock Enabled")
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { enabled ->
                        isEnabled = enabled
                        if (enabled) mockManager.enable() else mockManager.disable()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add new endpoint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            // Mock endpoints list
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

            // Clear all button
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