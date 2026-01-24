package com.tumba.bhaga.ui.screens.stockdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumba.bhaga.ui.components.BuySellDialog
import com.tumba.bhaga.ui.components.CompanyTag
import com.tumba.bhaga.ui.components.PercentChangeBadge
import com.tumba.bhaga.ui.components.StockNewsList
import kotlin.math.abs

@Composable
fun StockDetailScreen(
    ticker: String,
    viewModel: StockDetailViewModel = hiltViewModel()
) {
    val stock by viewModel.stock.collectAsState()
    val isFavourite by viewModel.isFavourite.collectAsState()
    val userBalance by viewModel.userBalance.collectAsState()
    val ownedShares by viewModel.ownedShares.collectAsState()
    val averagePrice by viewModel.averagePrice.collectAsState()
    val transactionSuccess by viewModel.transactionSuccess.collectAsState()
    val transactionError by viewModel.transactionError.collectAsState()

    var showBuyDialog by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }

    LaunchedEffect(ticker) {
        viewModel.loadStock(ticker)
    }

    LaunchedEffect(transactionSuccess, transactionError) {
        transactionSuccess?.let {
            viewModel.clearTransactionMessages()
        }
        transactionError?.let {
            viewModel.clearTransactionMessages()
        }
    }

    if (stock == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Calculate profit/loss
        val currentPrice = stock!!.currentPrice
        val totalInvested = ownedShares * averagePrice
        val currentValue = ownedShares * currentPrice
        val profitLoss = currentValue - totalInvested
        val profitLossPercent = if (totalInvested > 0) {
            ((currentValue - totalInvested) / totalInvested) * 100
        } else 0.0
        val isProfitable = profitLoss >= 0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompanyTag(
                            stock!!.ticker,
                            stock!!.companyName,
                            stock!!.logoUrl,
                            Modifier.fillMaxHeight()
                        )

                        Spacer(Modifier.weight(1f))

                        IconButton(
                            onClick = {
                                if (isFavourite) viewModel.removeFromFavourites()
                                else viewModel.addToFavourites()
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavourite) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                                tint = if (isFavourite) Color.Yellow else Color.Gray
                            )
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                "$${"%.2f".format(stock!!.currentPrice)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                "${if (stock!!.isPositiveChange) "+" else "-"}${
                                    " $%.2f".format(abs(stock!!.priceChange))
                                }",
                                color = Color(
                                    when (stock!!.isPositiveChange) {
                                        true -> ColorUtils.blendARGB(
                                            Color.Green.toArgb(),
                                            Color.Black.toArgb(),
                                            0.4f
                                        )
                                        false -> ColorUtils.blendARGB(
                                            Color.Red.toArgb(),
                                            Color.Black.toArgb(),
                                            0.2f
                                        )
                                    }
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        PercentChangeBadge(
                            stock!!.percentChange,
                            stock!!.isPositiveChange,
                            Modifier.padding(8.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showBuyDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Buy")
                    }

                    Button(
                        onClick = { showSellDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = ownedShares > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Sell")
                    }
                }

                if (ownedShares > 0) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isProfitable) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Your Position",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))

                            InfoRow("Owned Shares", ownedShares.toString())
                            InfoRow("Average Price", "$${"%.2f".format(averagePrice)}")
                            InfoRow("Total Invested", "$${"%.2f".format(totalInvested)}")
                            InfoRow("Current Value", "$${"%.2f".format(currentValue)}")

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Profit/Loss:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${if (isProfitable) "+" else ""}$${"%.2f".format(profitLoss)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isProfitable) {
                                            Color(0xFF4CAF50)
                                        } else {
                                            Color(0xFFF44336)
                                        }
                                    )
                                    Text(
                                        "${if (isProfitable) "+" else ""}${"%.2f".format(profitLossPercent)}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isProfitable) {
                                            Color(0xFF4CAF50)
                                        } else {
                                            Color(0xFFF44336)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow("Open", "$${"%.2f".format(stock!!.openPrice)}")
                        InfoRow("High", "$${"%.2f".format(stock!!.highPrice)}")
                        InfoRow("Low", "$${"%.2f".format(stock!!.lowPrice)}")
                        InfoRow("Previous Close", "$${"%.2f".format(stock!!.previousClosePrice)}")
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        stock!!.website?.let { InfoRow("Website", it) }
                        stock!!.industry?.let { InfoRow("Industry", it) }
                        stock!!.exchange?.let { InfoRow("Exchange", it) }
                        stock!!.country?.let { InfoRow("Country", it) }
                        stock!!.currency?.let { InfoRow("Currency", it) }
                    }
                }
            }

            StockNewsList(stock!!.newsList, modifier = Modifier.padding(vertical = 0.dp))
        }

        if (showBuyDialog) {
            BuySellDialog(
                isBuy = true,
                ticker = stock!!.ticker,
                companyName = stock!!.companyName,
                currentPrice = stock!!.currentPrice,
                currentBalance = userBalance,
                ownedShares = ownedShares,
                averagePrice = averagePrice,
                onDismiss = { showBuyDialog = false },
                onConfirm = { quantity ->
                    viewModel.buyStock(quantity)
                    showBuyDialog = false
                }
            )
        }

        if (showSellDialog) {
            BuySellDialog(
                isBuy = false,
                ticker = stock!!.ticker,
                companyName = stock!!.companyName,
                currentPrice = stock!!.currentPrice,
                currentBalance = userBalance,
                ownedShares = ownedShares,
                averagePrice = averagePrice,
                onDismiss = { showSellDialog = false },
                onConfirm = { quantity ->
                    viewModel.sellStock(quantity)
                    showSellDialog = false
                }
            )
        }

        transactionSuccess?.let { message ->
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(message)
            }
        }

        transactionError?.let { message ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Text(message)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}