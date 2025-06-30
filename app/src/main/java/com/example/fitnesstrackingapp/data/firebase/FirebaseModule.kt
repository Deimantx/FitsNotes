package com.example.fitnesstrackingapp.data.firebase // Adjust package name as needed

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * A simple object to provide Firebase service instances.
 * In a larger app, you might use a proper DI framework like Hilt or Koin.
 */
object FirebaseModule {

    /**
     * Provides an instance of FirebaseAuth.
     */
    val authInstance: FirebaseAuth by lazy {
        Firebase.auth
    }

    /**
     * Provides an instance of FirebaseFirestore.
     */
    val firestoreInstance: FirebaseFirestore by lazy {
        Firebase.firestore.apply {
            // Optional: Configure Firestore settings if needed
            // For example, to enable offline persistence (cache):
            // val settings = FirebaseFirestoreSettings.Builder()
            //     .setPersistenceEnabled(true)
            //     .build()
            // this.firestoreSettings = settings
            //
            // Or for local emulator testing:
            // this.useEmulator("10.0.2.2", 8080) // 10.0.2.2 is localhost for Android emulator
            // Firebase.auth.useEmulator("10.0.2.2", 9099) // For auth emulator
        }
    }

    // You can add other Firebase services here as needed, e.g.:
    // val storageInstance: FirebaseStorage by lazy { Firebase.storage }
    // val functionsInstance: FirebaseFunctions by lazy { Firebase.functions }
    // val analyticsInstance: FirebaseAnalytics by lazy { Firebase.analytics }

    /**
     * Checks if a user is currently signed in.
     * This is just a utility function example.
     */
    fun isUserAuthenticated(): Boolean {
        return authInstance.currentUser != null
    }
}

// How to use:
// In your Repositories or ViewModels, you would access these instances like:
// val auth = FirebaseModule.authInstance
// val db = FirebaseModule.firestoreInstance
//
// Example of enabling Firestore offline persistence in an Application class:
/*
package com.example.fitnesstrackingapp

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FitnessApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable Firestore offline persistence
        // This should ideally be done once, e.g., in Application class or early in MainActivity.
        // val firestore = FirebaseFirestore.getInstance()
        // val settings = FirebaseFirestoreSettings.Builder(firestore.firestoreSettings)
        //     .setPersistenceEnabled(true)
        //     // .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED) // Optional
        //     .build()
        // firestore.firestoreSettings = settings
        // Log.d("FitnessApp", "Firestore persistence enabled.")

        // If using FirebaseModule and settings are applied there, this might not be needed here,
        // but this is a common place to initialize app-wide components.
    }
}

// Remember to register this Application class in your AndroidManifest.xml:
// <application
// android:name=".FitnessApp"
// ... >
// </application>
*/
