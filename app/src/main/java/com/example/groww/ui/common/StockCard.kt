package com.example.groww.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groww.data.model.network.StockInfo

@Composable
fun StockCard(stock: StockInfo) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .border(1.dp, Color.Gray)
            .padding(16.dp)
            .width(IntrinsicSize.Max)
    ) {
        Text(
            text = stock.ticker,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "$${stock.price}",
            fontSize = 16.sp,
            color = if (stock.changeAmount.startsWith("-")) Color.Red else Color.Green
        )
        Text(
            text = stock.changePercentage,
            fontSize = 12.sp,
            color = if (stock.changeAmount.startsWith("-")) Color.Red else Color.Green
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StockCardPreview() {
    StockCard(
        stock = StockInfo(
            ticker = "CIGL",
            price = "2.91",
            changeAmount = "1.47",
            changePercentage = "102.0833%",
            volume = "132481624"
        )
    )
}