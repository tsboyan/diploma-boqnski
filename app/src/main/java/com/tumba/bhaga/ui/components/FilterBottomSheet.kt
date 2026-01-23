package com.tumba.bhaga.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tumba.bhaga.domain.models.OwnershipFilter
import com.tumba.bhaga.domain.models.PerformanceFilter
import com.tumba.bhaga.domain.models.SearchFilters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentFilters: SearchFilters,
    availableIndustries: List<String>,
    availableExchanges: List<String>,
    availableCountries: List<String>,
    onDismiss: () -> Unit,
    onApplyFilters: (SearchFilters) -> Unit
) {
    var selectedIndustries by remember { mutableStateOf(currentFilters.industries) }
    var selectedExchanges by remember { mutableStateOf(currentFilters.exchanges) }
    var selectedCountries by remember { mutableStateOf(currentFilters.countries) }
    var performanceFilter by remember { mutableStateOf(currentFilters.performanceFilter) }
    var ownershipFilter by remember { mutableStateOf(currentFilters.ownershipFilter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Performance Filter
                item {
                    FilterSection(title = "Performance") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PerformanceFilter.entries.forEach { filter ->
                                FilterChip(
                                    selected = performanceFilter == filter,
                                    onClick = { performanceFilter = filter },
                                    label = { Text(filter.name.replace("_", " ")) }
                                )
                            }
                        }
                    }
                }

                // Ownership Filter
                item {
                    FilterSection(title = "Ownership") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OwnershipFilter.entries.forEach { filter ->
                                FilterChip(
                                    selected = ownershipFilter == filter,
                                    onClick = { ownershipFilter = filter },
                                    label = { Text(filter.name.replace("_", " ")) }
                                )
                            }
                        }
                    }
                }

                // Industry Filter
                if (availableIndustries.isNotEmpty()) {
                    item {
                        FilterSection(title = "Industry") {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableIndustries.forEach { industry ->
                                    FilterChip(
                                        selected = industry in selectedIndustries,
                                        onClick = {
                                            selectedIndustries = if (industry in selectedIndustries) {
                                                selectedIndustries - industry
                                            } else {
                                                selectedIndustries + industry
                                            }
                                        },
                                        label = { Text(industry) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Exchange Filter
                if (availableExchanges.isNotEmpty()) {
                    item {
                        FilterSection(title = "Exchange") {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableExchanges.forEach { exchange ->
                                    FilterChip(
                                        selected = exchange in selectedExchanges,
                                        onClick = {
                                            selectedExchanges = if (exchange in selectedExchanges) {
                                                selectedExchanges - exchange
                                            } else {
                                                selectedExchanges + exchange
                                            }
                                        },
                                        label = { Text(exchange) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Country Filter
                if (availableCountries.isNotEmpty()) {
                    item {
                        FilterSection(title = "Country") {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableCountries.forEach { country ->
                                    FilterChip(
                                        selected = country in selectedCountries,
                                        onClick = {
                                            selectedCountries = if (country in selectedCountries) {
                                                selectedCountries - country
                                            } else {
                                                selectedCountries + country
                                            }
                                        },
                                        label = { Text(country) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedIndustries = emptySet()
                        selectedExchanges = emptySet()
                        selectedCountries = emptySet()
                        performanceFilter = PerformanceFilter.ALL
                        ownershipFilter = OwnershipFilter.ALL
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }

                Button(
                    onClick = {
                        onApplyFilters(
                            SearchFilters(
                                industries = selectedIndustries,
                                exchanges = selectedExchanges,
                                countries = selectedCountries,
                                performanceFilter = performanceFilter,
                                ownershipFilter = ownershipFilter
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}