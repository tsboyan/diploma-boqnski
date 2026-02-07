package com.tumba.bhaga.data.di

import android.content.Context
import com.tumba.bhaga.data.local.StockDatabase
import com.tumba.bhaga.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): StockDatabase = StockDatabase.getInstance(context)

    @Provides
    fun provideStockDao(database: StockDatabase): StockDao {
        return database.stockDao()
    }

    @Provides
    fun provideFavouritesDao(database: StockDatabase): FavouritesDao {
        return database.favouritesDao()
    }

    @Provides
    fun provideInvalidationDao(database: StockDatabase): InvalidationDao {
        return database.invalidationDao()
    }

    @Provides
    fun provideNewsDao(database: StockDatabase): NewsDao {
        return database.newsDao()
    }

    @Provides
    fun provideSearchDao(database: StockDatabase): SearchDao {
        return database.searchDao()
    }

    @Provides
    fun provideUserDao(database: StockDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun providePortfolioDao(database: StockDatabase): PortfolioDao {
        return database.portfolioDao()
    }

    @Provides
    fun provideTransactionDao(database: StockDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideBlockedStockDao(database: StockDatabase): BlockedStockDao {
        return database.blockedStockDao()
    }
}