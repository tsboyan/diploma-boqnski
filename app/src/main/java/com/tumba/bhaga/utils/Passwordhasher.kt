package com.tumba.bhaga.utils


import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordHasher {

    /**
     * Hashes a plain text password using bcrypt.
     * Cost of 12 provides a good balance between security and performance.
     *
     * @param password The plain text password to hash
     * @return The bcrypt hashed password
     */
    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    /**
     * Verifies a plain text password against a bcrypt hash.
     *
     * @param password The plain text password to verify
     * @param hash The bcrypt hash to compare against
     * @return True if the password matches the hash, false otherwise
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }
}