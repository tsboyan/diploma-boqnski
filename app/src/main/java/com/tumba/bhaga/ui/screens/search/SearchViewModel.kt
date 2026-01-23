package com.tumba.bhaga.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumba.bhaga.data.repository.AuthRepository
import com.tumba.bhaga.data.repository.SearchRepository
import com.tumba.bhaga.domain.models.EnrichedSearchEntry
import com.tumba.bhaga.domain.models.OwnershipFilter
import com.tumba.bhaga.domain.models.PerformanceFilter
import com.tumba.bhaga.domain.models.SearchFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.apache.commons.text.similarity.LevenshteinDistance
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _entries = MutableStateFlow<List<EnrichedSearchEntry>>(emptyList())
    val entries: StateFlow<List<EnrichedSearchEntry>> = _entries

    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters

    private val _availableIndustries = MutableStateFlow<List<String>>(emptyList())
    val availableIndustries: StateFlow<List<String>> = _availableIndustries

    private val _availableExchanges = MutableStateFlow<List<String>>(emptyList())
    val availableExchanges: StateFlow<List<String>> = _availableExchanges

    private val _availableCountries = MutableStateFlow<List<String>>(emptyList())
    val availableCountries: StateFlow<List<String>> = _availableCountries

    init {
        loadSearchEntries()
        loadFilterOptions()
    }

    private fun loadSearchEntries() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            _entries.value = repository.getAllEnrichedSearchEntries(userId)
        }
    }

    private fun loadFilterOptions() {
        viewModelScope.launch {
            _availableIndustries.value = repository.getAvailableIndustries()
            _availableExchanges.value = repository.getAvailableExchanges()
            _availableCountries.value = repository.getAvailableCountries()
        }
    }

    fun updateFilters(newFilters: SearchFilters) {
        _filters.value = newFilters
    }

    fun getFilteredEntries(query: String): List<EnrichedSearchEntry> {
        val currentEntries = _entries.value
        val currentFilters = _filters.value

        if (currentEntries.isEmpty()) return emptyList()

        // Apply filters
        var filtered = currentEntries

        // Industry filter
        if (currentFilters.industries.isNotEmpty()) {
            filtered = filtered.filter { entry ->
                entry.industry in currentFilters.industries
            }
        }

        // Exchange filter
        if (currentFilters.exchanges.isNotEmpty()) {
            filtered = filtered.filter { entry ->
                entry.exchange in currentFilters.exchanges
            }
        }

        // Country filter
        if (currentFilters.countries.isNotEmpty()) {
            filtered = filtered.filter { entry ->
                entry.country in currentFilters.countries
            }
        }

        // Performance filter
        when (currentFilters.performanceFilter) {
            PerformanceFilter.GAINERS -> {
                filtered = filtered.filter { entry ->
                    (entry.percentChange ?: 0.0) > 0
                }
            }
            PerformanceFilter.LOSERS -> {
                filtered = filtered.filter { entry ->
                    (entry.percentChange ?: 0.0) < 0
                }
            }
            PerformanceFilter.ALL -> { /* No filter */ }
        }

        // Ownership filter
        when (currentFilters.ownershipFilter) {
            OwnershipFilter.OWNED -> {
                filtered = filtered.filter { it.isOwned }
            }
            OwnershipFilter.FAVORITES -> {
                filtered = filtered.filter { it.isFavorite }
            }
            OwnershipFilter.NOT_OWNED -> {
                filtered = filtered.filter { !it.isOwned }
            }
            OwnershipFilter.ALL -> { /* No filter */ }
        }

        // Apply search query using Levenshtein distance
        if (query.isNotEmpty()) {
            val q = query.lowercase()
            val ld = LevenshteinDistance.getDefaultInstance()

            filtered = filtered
                .map { entry ->
                    entry to min(
                        ld.apply(entry.ticker.lowercase(), q),
                        ld.apply(entry.companyName.lowercase(), q)
                    )
                }
                .sortedBy { it.second }
                .take(15)
                .map { it.first }
        }

        return filtered
    }

    fun clearFilters() {
        _filters.value = SearchFilters()
    }

    fun hasActiveFilters(): Boolean {
        val f = _filters.value
        return f.industries.isNotEmpty() ||
                f.exchanges.isNotEmpty() ||
                f.countries.isNotEmpty() ||
                f.performanceFilter != PerformanceFilter.ALL ||
                f.ownershipFilter != OwnershipFilter.ALL
    }
}