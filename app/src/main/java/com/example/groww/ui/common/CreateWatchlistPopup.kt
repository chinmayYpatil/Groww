package com.example.groww.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.groww.ui.theme.GrowwShapes

@Composable
fun CreateWatchlistPopup(
    onDismiss: () -> Unit,
    onWatchlistCreated: (String) -> Unit
) {
    var watchlistName by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(300.dp)
                .wrapContentHeight(),
            shape = GrowwShapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Create New Watchlist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = watchlistName,
                    onValueChange = {
                        watchlistName = it
                        showError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Watchlist Name") },
                    placeholder = { Text("e.g., Tech Stocks, My Portfolio") },
                    enabled = !isCreating,
                    singleLine = true,
                    isError = showError
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please enter a valid name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isCreating
                    ) {
                        Text("Cancel")
                    }

                    // Create Button
                    Button(
                        onClick = {
                            val trimmedName = watchlistName.trim()
                            if (trimmedName.isNotBlank()) {
                                isCreating = true
                                onWatchlistCreated(trimmedName)
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = watchlistName.trim().isNotBlank() && !isCreating
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}