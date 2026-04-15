package com.tumba.bhaga.data.local.seeding

import com.tumba.bhaga.data.local.dao.UserDao
import com.tumba.bhaga.data.local.entity.UserEntity
import com.tumba.bhaga.utils.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val userDao: UserDao
) {

    /**
     * Seeds the database with test users and admins
     * Call this once on app startup if needed
     *
     * Creates:
     * - 4 admin users
     * - 30 regular users
     * - All with hashed passwords
     */
    suspend fun seedDatabaseIfEmpty() {
        return withContext(Dispatchers.IO) {
            try {
                // Only seed if database is empty
                val existingUsers = userDao.getAllUsers()
                if (existingUsers.isNotEmpty()) {
                    // Database already has data, don't seed
                    return@withContext
                }

                // Seed admins
                seedAdminUsers()

                // Seed regular users
                seedRegularUsers()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun seedAdminUsers() {
        val adminUsers = listOf(
            UserEntity(
                name = "Admin One",
                email = "admin@bhaga.com",
                password = PasswordHasher.hashPassword("admin123"),
                balance = 50000.0
            ),
            UserEntity(
                name = "Admin Two",
                email = "superadmin@bhaga.com",
                password = PasswordHasher.hashPassword("admin123"),
                balance = 50000.0
            ),
            UserEntity(
                name = "System Admin",
                email = "admin.system@bhaga.com",
                password = PasswordHasher.hashPassword("admin123"),
                balance = 50000.0
            ),
            UserEntity(
                name = "Support Admin",
                email = "admin.support@bhaga.com",
                password = PasswordHasher.hashPassword("admin123"),
                balance = 50000.0
            )
        )

        adminUsers.forEach { user ->
            userDao.insertUser(user)
        }
    }

    private suspend fun seedRegularUsers() {
        val firstNames = listOf(
            "John", "Jane", "Michael", "Emily", "David", "Sarah", "James", "Emma",
            "Robert", "Olivia", "William", "Ava", "Richard", "Isabella", "Joseph",
            "Sophia", "Thomas", "Charlotte", "Charles", "Amelia", "Christopher",
            "Mia", "Daniel", "Harper", "Matthew", "Evelyn", "Mark", "Abigail",
            "Donald", "Ella"
        )

        val lastNames = listOf(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
            "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
            "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
            "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark",
            "Ramirez", "Lewis", "Robinson"
        )

        val companies = listOf(
            "Tech", "Finance", "Retail", "Healthcare", "Energy", "Transport",
            "Entertainment", "Education", "Manufacturing", "Real Estate"
        )

        // Create 30 regular users
        for (i in 0 until 30) {
            val firstName = firstNames[i % firstNames.size]
            val lastName = lastNames[i % lastNames.size]
            val company = companies[i % companies.size]
            val email = "${firstName.lowercase()}.${lastName.lowercase()}$i@example.com"

            val user = UserEntity(
                name = "$firstName $lastName",
                email = email,
                password = PasswordHasher.hashPassword("password123"), // Default password
                balance = (5000..100000).random().toDouble() // Random balance between 5k-100k
            )

            userDao.insertUser(user)
        }
    }
}