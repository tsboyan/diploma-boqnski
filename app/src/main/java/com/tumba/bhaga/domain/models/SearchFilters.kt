package com.tumba.bhaga.domain.models

data class SearchFilters(
    val industries: Set<String> = emptySet(),
    val exchanges: Set<String> = emptySet(),
    val countries: Set<String> = emptySet(),
    val performanceFilter: PerformanceFilter = PerformanceFilter.ALL,
    val ownershipFilter: OwnershipFilter = OwnershipFilter.ALL
)

enum class PerformanceFilter {
    ALL,
    GAINERS,    // percentChange > 0
    LOSERS      // percentChange < 0
}

enum class OwnershipFilter {
    ALL,
    OWNED,      // User owns this stock
    FAVORITES,  // User favorited this stock
    NOT_OWNED   // User doesn't own this stock
}