package com.example.groww.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groww.data.model.network.StockInfo
import com.example.groww.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockCard(
    stock: StockInfo,
    onClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPositive = !stock.changeAmount.startsWith("-")
    val changeColor = if (isPositive) PositiveGreen else NegativeRed
    val trendIcon = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Card(
        onClick = { onClick(stock.ticker) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stock Symbol and Company Initial
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stock.ticker,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Company Initial Circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stock.ticker.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Price Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "$${stock.price}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Change Amount and Percentage
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = if (isPositive) "Trending Up" else "Trending Down",
                            tint = changeColor,
                            modifier = Modifier.size(14.dp)
                        )

                        Text(
                            text = "${if (isPositive) "+" else ""}${stock.changeAmount}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = changeColor
                        )

                        Text(
                            text = "(${stock.changePercentage})",
                            style = MaterialTheme.typography.bodySmall,
                            color = changeColor
                        )
                    }
                }
            }

            // Volume (if available)
            if (stock.volume.isNotEmpty()) {
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Volume",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatVolume(stock.volume),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun formatVolume(volume: String): String {
    return try {
        val volumeNum = volume.toLongOrNull() ?: return volume
        when {
            volumeNum >= 1_000_000_000 -> "${(volumeNum / 1_000_000_000.0).format(1)}B"
            volumeNum >= 1_000_000 -> "${(volumeNum / 1_000_000.0).format(1)}M"
            volumeNum >= 1_000 -> "${(volumeNum / 1_000.0).format(1)}K"
            else -> volume
        }
    } catch (e: Exception) {
        volume
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

@Preview(showBackground = true)
@Composable
fun StockCardPreview() {
    GrowwTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StockCard(
                stock = StockInfo(
                    ticker = "AAPL",
                    price = "175.43",
                    changeAmount = "2.15",
                    changePercentage = "1.24%",
                    volume = "54832000"
                )
            )

            StockCard(
                stock = StockInfo(
                    ticker = "TSLA",
                    price = "195.89",
                    changeAmount = "-8.76",
                    changePercentage = "-4.28%",
                    volume = "98234567"
                )
            )
        }
    }
}