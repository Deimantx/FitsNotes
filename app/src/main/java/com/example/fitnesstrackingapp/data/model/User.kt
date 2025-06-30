package com.example.fitnesstrackingapp.data.model // Adjust package name as needed

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data class representing a user in the application.
 * This model can be used for storing user details in Firestore.
 *
 * @property uid The unique ID of the user, typically from Firebase Authentication.
 * @property email The email address of the user.
 * @property displayName Optional display name for the user.
 * @property createdAt Timestamp of when the user account was created in Firestore.
 * @property lastLogin Timestamp of the user's last login (can be updated).
 */
data class User(
    val uid: String = "", // Default empty for Firestore deserialization
    val email: String? = null,
    val displayName: String? = null,
    @ServerTimestamp // Automatically sets the timestamp on the server on creation
    val createdAt: Date? = null,
    @ServerTimestamp // Automatically updates the timestamp on the server on update
    var lastLogin: Date? = null, // Example of a mutable field
    // Add other user-specific fields as needed, e.g.:
    // val photoUrl: String? = null,
    // val健身目标: String? = null, // fitnessGoal
    // val preferredUnits: String? = "kg" // "kg" or "lbs"
) {
    // No-argument constructor is required by Firestore for deserialization
    constructor() : this("", null, null, null, null)
}
