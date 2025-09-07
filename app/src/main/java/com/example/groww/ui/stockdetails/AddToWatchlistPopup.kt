package com.example.groww.ui.stockdetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.groww.data.local.database.entities.WatchlistEntity
import com.example.groww.ui.theme.GrowwShapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToWatchlistBottomSheet(
    symbol: String,
    stockName: String,
    onDismiss: () -> Unit,
    viewModel: AddToWatchlistViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val watchlists by viewModel.watchlists.observeAsState(initial = emptyList())
    val selectedWatchlistIds by viewModel.selectedWatchlists.observeAsState(initial = emptySet())
    val newWatchlistName by viewModel.newWatchlistName.observeAsState(initial = "")
    val isAdding by viewModel.isAdding.observeAsState(initial = false)
    val actionStatus by viewModel.actionStatus.observeAsState()

    var existingWatchlistIds by remember { mutableStateOf(setOf<Long>()) }

    LaunchedEffect(symbol, watchlists) {
        existingWatchlistIds = emptySet() // implement real check if available
    }

    LaunchedEffect(actionStatus) {
        if (actionStatus != null && (actionStatus!!.contains("Added") || actionStatus!!.contains("Already"))) {
            kotlinx.coroutines.delay(1500)
            scope.launch { sheetState.hide() }
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = GrowwShapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Add to Watchlist",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Adding: $stockName ($symbol)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newWatchlistName,
                    onValueChange = { viewModel.onNewWatchlistNameChange(it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("New Watchlist Name") },
                    placeholder = { Text("e.g., Tech Stocks") },
                    enabled = !isAdding,
                    singleLine = true
                )

                Button(
                    onClick = { viewModel.addStockToWatchlists(symbol, stockName) },
                    enabled = newWatchlistName.isNotBlank() && !isAdding
                ) {
                    Text("Add")
                }
            }

            if (watchlists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(watchlists) { watchlist ->
                        val isAlreadyInWatchlist = existingWatchlistIds.contains(watchlist.id)
                        WatchlistCheckboxItem(
                            watchlist = watchlist,
                            isChecked = selectedWatchlistIds.contains(watchlist.id),
                            onCheckChanged = {
                                if (!isAlreadyInWatchlist) {
                                    viewModel.onWatchlistSelected(watchlist.id)
                                }
                            },
                            enabled = !isAdding && !isAlreadyInWatchlist,
                            isAlreadyAdded = isAlreadyInWatchlist
                        )
                    }
                }
            }

            actionStatus?.let { status ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (status.contains("Error") || status.contains("failed")) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        }
                    ),
                    shape = GrowwShapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!status.contains("Error") && !status.contains("failed")) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (status.contains("Error") || status.contains("failed")) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    scope.launch { sheetState.hide() }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAdding
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun WatchlistCheckboxItem(
    watchlist: WatchlistEntity,
    isChecked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    enabled: Boolean = true,
    isAlreadyAdded: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckChanged(!isChecked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAlreadyAdded) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Already added",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckChanged,
                enabled = enabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = watchlist.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isAlreadyAdded) FontWeight.Medium else FontWeight.Normal
            )

            if (isAlreadyAdded) {
                Text(
                    text = "Already added",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
