package com.tumba.bhaga.data.repository

import com.tumba.bhaga.data.local.StockDatabase
import com.tumba.bhaga.domain.models.EnrichedSearchEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(private val db: StockDatabase) {
    suspend fun getAllEnrichedSearchEntries(userId: Long?): List<EnrichedSearchEntry> = withContext(Dispatchers.IO) {
        val searchEntries = db.searchDao().getAllSearchEntries()

        searchEntries.map { searchEntry ->
            // Get company profile data
            val profile = db.stockDao().getCompanyWithQuote(searchEntry.ticker)

            // Check if owned
            val isOwned = userId?.let {
                db.portfolioDao().getPortfolioItem(it, searchEntry.ticker) != null
            } ?: false

            // Check if favorite
            val isFavorite = db.favouritesDao().isFavourite(searchEntry.ticker)

            EnrichedSearchEntry(
                ticker = searchEntry.ticker,
                companyName = searchEntry.companyName,
                logoUrl = searchEntry.logoUrl,
                industry = profile?.profile?.industry,
                exchange = profile?.profile?.exchange,
                country = profile?.profile?.country,
                currentPrice = profile?.quote?.currentPrice,
                percentChange = profile?.quote?.percentChange,
                isOwned = isOwned,
                isFavorite = isFavorite
            )
        }
    }

    suspend fun getAvailableIndustries(): List<String> = withContext(Dispatchers.IO) {
        db.searchDao().getAllSearchEntries()
            .mapNotNull {
                db.stockDao().getCompanyWithQuote(it.ticker)?.profile?.industry
            }
            .distinct()
            .sorted()
    }

    suspend fun getAvailableExchanges(): List<String> = withContext(Dispatchers.IO) {
        db.searchDao().getAllSearchEntries()
            .mapNotNull {
                db.stockDao().getCompanyWithQuote(it.ticker)?.profile?.exchange
            }
            .distinct()
            .sorted()
    }

    suspend fun getAvailableCountries(): List<String> = withContext(Dispatchers.IO) {
        db.searchDao().getAllSearchEntries()
            .mapNotNull {
                db.stockDao().getCompanyWithQuote(it.ticker)?.profile?.country
            }
            .distinct()
            .sorted()
    }
}