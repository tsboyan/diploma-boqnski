package com.tumba.bhaga

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.tumba.bhaga.data.local.seeding.DatabaseSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Application class for Bhaga Stock Trading App
 *
 * Handles:
 * - Hilt dependency injection initialization (@HiltAndroidApp)
 * - Database seeding with test users on app startup
 * - Global app instance access
 */
@HiltAndroidApp
class BhagaApp : Application() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    companion object {
        lateinit var instance: BhagaApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Seed database with test data (30 users + 4 admins)
        // This only runs if the database is empty, so it won't duplicate on subsequent app launches
        CoroutineScope(Dispatchers.IO).launch {
            databaseSeeder.seedDatabaseIfEmpty()
        }
    }
}