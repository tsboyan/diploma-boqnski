package com.tumba.bhaga.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumba.bhaga.data.local.entity.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: AdminPanelViewModel = hiltViewModel()
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val blockedStocks by viewModel.blockedStocks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showBalanceDialog by remember { mutableStateOf(false) }
    var showBlockStockDialog by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf<Long?>(null) }

    val tabs = listOf("Users", "Transactions", "Blocked Stocks")

    LaunchedEffect(successMessage, errorMessage) {
        if (successMessage != null || errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Admin Panel",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Messages
        successMessage?.let {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        errorMessage?.let {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        when (selectedTab) {
            0 -> UsersTab(
                users = allUsers,
                onAdjustBalance = { userId ->
                    selectedUserId = userId
                    showBalanceDialog = true
                }
            )
            1 -> TransactionsTab(transactions = allTransactions)
            2 -> BlockedStocksTab(
                blockedStocks = blockedStocks,
                onBlockStock = { showBlockStockDialog = true },
                onUnblockStock = { viewModel.unblockStock(it) }
            )
        }
    }

    // Balance Adjustment Dialog
    if (showBalanceDialog && selectedUserId != null) {
        BalanceAdjustmentDialog(
            onDismiss = { showBalanceDialog = false },
            onConfirm = { amount ->
                viewModel.adjustUserBalance(selectedUserId!!, amount)
                showBalanceDialog = false
            }
        )
    }

    // Block Stock Dialog
    if (showBlockStockDialog) {
        BlockStockDialog(
            onDismiss = { showBlockStockDialog = false },
            onConfirm = { ticker, reason ->
                viewModel.blockStock(ticker, reason)
                showBlockStockDialog = false
            }
        )
    }
}

@Composable
private fun UsersTab(
    users: List<com.tumba.bhaga.data.local.entity.UserEntity>,
    onAdjustBalance: (Long) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Balance: $${"%.2f".format(user.balance)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onAdjustBalance(user.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Adjust Balance"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionsTab(
    transactions: List<com.tumba.bhaga.data.local.entity.TransactionEntity>
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions) { transaction ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (transaction.type == TransactionType.BUY) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = transaction.ticker,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = transaction.type.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (transaction.type == TransactionType.BUY) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${transaction.quantity} shares @ $${"%.2f".format(transaction.pricePerShare)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Total: $${"%.2f".format(transaction.totalAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat.format(Date(transaction.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockedStocksTab(
    blockedStocks: List<com.tumba.bhaga.data.local.entity.BlockedStockEntity>,
    onBlockStock: () -> Unit,
    onUnblockStock: (String) -> Unit
) {
    Column {
        Button(
            onClick = onBlockStock,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Block New Stock")
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(blockedStocks) { stock ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stock.ticker,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reason: ${stock.reason}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Blocked by: ${stock.blockedBy}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onUnblockStock(stock.ticker) }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Unblock",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceAdjustmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust User Balance") },
        text = {
            Column {
                Text("Enter amount (positive to add, negative to subtract)")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("$") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amount.toDoubleOrNull()?.let { onConfirm(it) }
                },
                enabled = amount.toDoubleOrNull() != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BlockStockDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var ticker by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Block Stock") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ticker,
                    onValueChange = { ticker = it.uppercase() },
                    label = { Text("Stock Ticker") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(ticker, reason) },
                enabled = ticker.isNotBlank() && reason.isNotBlank()
            ) {
                Text("Block")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}