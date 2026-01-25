package com.tumba.bhaga.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumba.bhaga.data.local.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isTokenValid by viewModel.isTokenValid.collectAsState()
    var tokenValidityEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val tokenInitial by viewModel.tokenInitial.collectAsState()
    var token by remember { mutableStateOf("") }

    val currentThemeMode by viewModel.themeMode.collectAsState()

    LaunchedEffect(tokenInitial) {
        token = tokenInitial
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Theme Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)

            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentThemeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentThemeMode == mode,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (mode) {
                                    ThemeMode.LIGHT -> "Light Mode"
                                    ThemeMode.DARK -> "Dark Mode"
                                    ThemeMode.SYSTEM -> "System Default"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = when (mode) {
                                    ThemeMode.LIGHT -> "Always use light theme"
                                    ThemeMode.DARK -> "Always use dark theme"
                                    ThemeMode.SYSTEM -> "Follow system settings"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        // Cache Management Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Cache Management", style = MaterialTheme.typography.titleMedium)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CacheRow("Stock data") { viewModel.clearStockCache() }
                CacheRow("Company data") { viewModel.clearCompanyCache() }
                CacheRow("News data") { viewModel.clearNewsCache() }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        // API Token Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("API Token", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Enter API token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row {
                Spacer(Modifier.weight(1f))

                Button(
                    enabled = tokenValidityEnabled,
                    onClick = {
                        tokenValidityEnabled = false
                        viewModel.resetNewToken()
                        token = tokenInitial
                        tokenValidityEnabled = true
                    }
                ) {
                    Text("Reset")
                }

                Spacer(Modifier.width(16.dp))

                Button(
                    enabled = tokenValidityEnabled,
                    onClick = {
                        tokenValidityEnabled = false

                        viewModel.checkTokenValidity(token)
                        if (isTokenValid == true) {
                            viewModel.setNewToken(token)
                        } else {
                            Toast.makeText(
                                context,
                                "Invalid token",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        tokenValidityEnabled = true
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun CacheRow(label: String, onClear: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        IconButton(onClick = onClear) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Clear $label"
            )
        }
    }
}