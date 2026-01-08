package com.tumba.bhaga.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumba.bhaga.data.repository.SearchRepository
import com.tumba.bhaga.domain.models.SearchEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.apache.commons.text.similarity.LevenshteinDistance
import javax.inject.Inject
import kotlin.collections.map
import kotlin.math.min

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository
) : ViewModel() {

    private val _entries = MutableStateFlow<List<SearchEntry>>(emptyList())
    val entries: StateFlow<List<SearchEntry>> = _entries

    init {
        getAllSearchEntries()
    }

    private fun getAllSearchEntries() {
        viewModelScope.launch {
            _entries.value = repository.getAllSearchEntries()
        }
    }

    fun getFilteredEntries(query: String): List<SearchEntry> {
        val currentEntries = _entries.value

        // Return empty if query is empty OR if there are no entries
        if (query.isEmpty() || currentEntries.isEmpty()) {
            return emptyList()
        }

        val q = query.lowercase()
        val ld = LevenshteinDistance.getDefaultInstance()

        return currentEntries
            .map {
                it to min(
                    ld.apply(it.ticker.lowercase(), q),
                    ld.apply(it.companyName.lowercase(), q)
                )
            }
            .sortedBy { it.second }
            .take(15)
            .map { it.first }
    }
}