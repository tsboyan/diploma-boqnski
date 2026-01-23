package com.tumba.bhaga.domain.models

data class EnrichedSearchEntry(
    val ticker: String,
    val companyName: String,
    val logoUrl: String,
    val industry: String?,
    val exchange: String?,
    val country: String?,
    val currentPrice: Double?,
    val percentChange: Double?,
    val isOwned: Boolean,
    val isFavorite: Boolean
)

// Keep the old SearchEntry for backwards compatibility
fun EnrichedSearchEntry.toSearchEntry(): SearchEntry {
    return SearchEntry(
        ticker = ticker,
        companyName = companyName,
        logoUrl = logoUrl
    )
}