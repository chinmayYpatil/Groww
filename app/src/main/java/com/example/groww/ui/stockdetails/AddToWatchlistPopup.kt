package com.example.groww.ui.stockdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.groww.data.local.database.entities.WatchlistEntity
import com.example.groww.ui.theme.GrowwTheme
import androidx.compose.ui.graphics.Color
import com.example.groww.ui.theme.GrowwShapes

@Composable
fun AddToWatchlistPopup(
    symbol: String,
    stockName: String,
    onDismiss: () -> Unit,
    viewModel: AddToWatchlistViewModel = hiltViewModel()
) {
    val watchlists by viewModel.watchlists.observeAsState(initial = emptyList())
    val selectedWatchlists by viewModel.selectedWatchlists.observeAsState(initial = emptySet())
    val newWatchlistName by viewModel.newWatchlistName.observeAsState(initial = "")
    val isAdding by viewModel.isAdding.observeAsState(initial = false)
    val actionStatus by viewModel.actionStatus.observeAsState()

    if (actionStatus != null) {
        // You could use a Snackbar or a Toast here
        onDismiss()
    }

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
                    text = "Add to Watchlist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Create New Watchlist section
                OutlinedTextField(
                    value = newWatchlistName,
                    onValueChange = { viewModel.onNewWatchlistNameChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("New Watchlist Name") },
                    trailingIcon = {
                        Button(
                            onClick = {
                                if (newWatchlistName.isNotBlank()) {
                                    viewModel.addStockToWatchlists(symbol, stockName)
                                }
                            },
                            enabled = newWatchlistName.isNotBlank() && !isAdding
                        ) {
                            Text("Add")
                        }
                    }
                )

                if (watchlists.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Or select existing watchlists",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Existing Watchlists list
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(watchlists) { watchlist ->
                            WatchlistCheckboxItem(
                                watchlist = watchlist,
                                isChecked = selectedWatchlists.contains(watchlist.id),
                                onCheckChanged = { viewModel.onWatchlistSelected(watchlist.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.addStockToWatchlists(symbol, stockName) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedWatchlists.isNotEmpty() && !isAdding
                    ) {
                        if (isAdding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Add to Selected")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistCheckboxItem(
    watchlist: WatchlistEntity,
    isChecked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckChanged(!isChecked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckChanged,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = watchlist.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}