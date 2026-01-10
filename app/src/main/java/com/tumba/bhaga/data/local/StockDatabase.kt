package com.tumba.bhaga.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tumba.bhaga.data.local.dao.FavouritesDao
import com.tumba.bhaga.data.local.dao.InvalidationDao
import com.tumba.bhaga.data.local.dao.NewsDao
import com.tumba.bhaga.data.local.dao.PortfolioDao
import com.tumba.bhaga.data.local.dao.SearchDao
import com.tumba.bhaga.data.local.dao.StockDao
import com.tumba.bhaga.data.local.dao.TransactionDao
import com.tumba.bhaga.data.local.dao.UserDao
import com.tumba.bhaga.data.local.entity.CompanyNewsEntity
import com.tumba.bhaga.data.local.entity.CompanyProfileEntity
import com.tumba.bhaga.data.local.entity.FavouriteEntity
import com.tumba.bhaga.data.local.entity.PortfolioEntity
import com.tumba.bhaga.data.local.entity.QuoteEntity
import com.tumba.bhaga.data.local.entity.SearchEntryEntity
import com.tumba.bhaga.data.local.entity.TransactionEntity
import com.tumba.bhaga.data.local.entity.UserEntity
import java.io.FileOutputStream

@Database(
    entities = [
        CompanyProfileEntity::class,
        QuoteEntity::class,
        CompanyNewsEntity::class,
        FavouriteEntity::class,
        SearchEntryEntity::class,
        UserEntity::class,
        PortfolioEntity::class,
        TransactionEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(TransactionTypeConverter::class)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao

    abstract fun favouritesDao(): FavouritesDao

    abstract fun invalidationDao(): InvalidationDao

    abstract fun newsDao(): NewsDao

    abstract fun searchDao(): SearchDao

    abstract fun userDao(): UserDao

    abstract fun portfolioDao(): PortfolioDao

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: StockDatabase? = null

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL,
                        password TEXT NOT NULL,
                        balance REAL NOT NULL DEFAULT 10000.0,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add balance column if upgrading from version 2 without it
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL,
                        password TEXT NOT NULL,
                        balance REAL NOT NULL DEFAULT 10000.0,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO user_new (id, name, email, password, balance, createdAt)
                    SELECT id, name, email, password, 10000.0, createdAt FROM user
                """.trimIndent())

                database.execSQL("DROP TABLE user")
                database.execSQL("ALTER TABLE user_new RENAME TO user")

                // Create portfolio table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS portfolio (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        ticker TEXT NOT NULL,
                        companyName TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        averagePrice REAL NOT NULL,
                        lastUpdated INTEGER NOT NULL,
                        FOREIGN KEY(userId) REFERENCES user(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                database.execSQL("CREATE INDEX IF NOT EXISTS index_portfolio_userId ON portfolio(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_portfolio_ticker ON portfolio(ticker)")

                // Create transaction table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `transaction` (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        ticker TEXT NOT NULL,
                        companyName TEXT NOT NULL,
                        type TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        pricePerShare REAL NOT NULL,
                        totalAmount REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        FOREIGN KEY(userId) REFERENCES user(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                database.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_userId ON `transaction`(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_ticker ON `transaction`(ticker)")
            }
        }

        fun getInstance(context: Context): StockDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbName = "search_entry.db"
                val dbPath = context.getDatabasePath(dbName)

                if (!dbPath.exists()) {
                    dbPath.parentFile?.mkdirs()
                    context.assets.open(dbName).use { input ->
                        FileOutputStream(dbPath).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockDatabase::class.java,
                    dbName
                )
                    .createFromFile(dbPath)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}