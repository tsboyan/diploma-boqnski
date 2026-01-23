package com.tumba.bhaga.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumba.bhaga.domain.models.EnrichedSearchEntry
import com.tumba.bhaga.ui.components.FilterBottomSheet
import com.tumba.bhaga.ui.components.SearchStockList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onStockClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val listState = rememberLazyListState()
    val filtered = remember { mutableStateListOf<EnrichedSearchEntry>() }
    var query by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }

    val filters by viewModel.filters.collectAsState()
    val availableIndustries by viewModel.availableIndustries.collectAsState()
    val availableExchanges by viewModel.availableExchanges.collectAsState()
    val availableCountries by viewModel.availableCountries.collectAsState()
    val hasActiveFilters = viewModel.hasActiveFilters()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = 8.dp,
                start = 8.dp,
                end = 8.dp
            )
    ) {
        // Search bar with filter button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search Stock ...") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            BadgedBox(
                badge = {
                    if (hasActiveFilters) {
                        Badge { Text("•") }
                    }
                }
            ) {
                FilledTonalIconButton(
                    onClick = { showFilterSheet = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter"
                    )
                }
            }
        }

        // Active filter chips
        if (hasActiveFilters) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.industries.take(2).forEach { industry ->
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text(industry, maxLines = 1) }
                    )
                }
                if (filters.industries.size > 2 ||
                    filters.exchanges.isNotEmpty() ||
                    filters.countries.isNotEmpty() ||
                    filters.performanceFilter.name != "ALL" ||
                    filters.ownershipFilter.name != "ALL") {
                    FilterChip(
                        selected = true,
                        onClick = { showFilterSheet = true },
                        label = { Text("+${countActiveFilters(filters) - 2}") }
                    )
                }
            }
        }

        LaunchedEffect(query, filters) {
            delay(300) // Debounce search
            filtered.clear()
            filtered.addAll(
                viewModel.getFilteredEntries(query)
            )

            if (filtered.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }

        SearchStockList(
            filtered.map { it.toSearchEntry() },
            onStockClick,
            listState
        )
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilters = filters,
            availableIndustries = availableIndustries,
            availableExchanges = availableExchanges,
            availableCountries = availableCountries,
            onDismiss = { showFilterSheet = false },
            onApplyFilters = { newFilters ->
                viewModel.updateFilters(newFilters)
            }
        )
    }
}

private fun countActiveFilters(filters: com.tumba.bhaga.domain.models.SearchFilters): Int {
    var count = 0
    count += filters.industries.size
    count += filters.exchanges.size
    count += filters.countries.size
    if (filters.performanceFilter.name != "ALL") count++
    if (filters.ownershipFilter.name != "ALL") count++
    return count
}

// Extension function to convert EnrichedSearchEntry to SearchEntry for display
private fun EnrichedSearchEntry.toSearchEntry(): com.tumba.bhaga.domain.models.SearchEntry {
    return com.tumba.bhaga.domain.models.SearchEntry(
        ticker = ticker,
        companyName = companyName,
        logoUrl = logoUrl
    )
}