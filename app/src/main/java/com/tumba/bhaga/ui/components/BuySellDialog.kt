package com.tumba.bhaga.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun BuySellDialog(
    isBuy: Boolean,
    ticker: String,
    companyName: String,
    currentPrice: Double,
    currentBalance: Double,
    ownedShares: Int,
    averagePrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int) -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    val quantityInt = quantity.toIntOrNull() ?: 0
    val totalCost = quantityInt * currentPrice

    val isValid = if (isBuy) {
        quantityInt > 0 && totalCost <= currentBalance
    } else {
        quantityInt > 0 && quantityInt <= ownedShares
    }

    // Calculate potential profit/loss for selling
    val potentialProceeds = if (!isBuy && quantityInt > 0) {
        quantityInt * currentPrice
    } else 0.0

    val costBasis = if (!isBuy && quantityInt > 0) {
        quantityInt * averagePrice
    } else 0.0

    val potentialProfitLoss = potentialProceeds - costBasis
    val isProfitable = potentialProfitLoss >= 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isBuy) "Buy $ticker" else "Sell $ticker",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow("Current Price", "$${"%.2f".format(currentPrice)}")
                    InfoRow("Your Balance", "$${"%.2f".format(currentBalance)}")
                    if (!isBuy) {
                        InfoRow("Owned Shares", ownedShares.toString())
                        InfoRow("Average Price", "$${"%.2f".format(averagePrice)}")
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (quantityInt > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total ${if (isBuy) "Cost" else "Proceeds"}:",
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "$${"%.2f".format(totalCost)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBuy) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }

                            // Show profit/loss for selling
                            if (!isBuy) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Cost Basis:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "$${"%.2f".format(costBasis)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Profit/Loss:",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${if (isProfitable) "+" else ""}$${"%.2f".format(potentialProfitLoss)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isProfitable) Color(0xFF4CAF50) else Color(0xFFF44336)
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isValid && quantityInt > 0) {
                    Text(
                        text = if (isBuy) "Insufficient funds" else "Insufficient shares",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onConfirm(quantityInt) },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBuy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(if (isBuy) "Buy" else "Sell")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}