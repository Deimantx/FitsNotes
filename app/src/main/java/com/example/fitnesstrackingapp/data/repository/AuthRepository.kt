package com.example.fitnesstrackingapp.data.repository // Adjust package name as needed

import com.example.fitnesstrackingapp.data.firebase.FirebaseModule
import com.example.fitnesstrackingapp.data.model.User
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

// A sealed class to represent the result of an authentication operation,
// which can be either success or failure.
sealed class AuthResultWrapper<out T> {
    data class Success<out T>(val data: T) : AuthResultWrapper<T>()
    data class Error(val exception: Exception) : AuthResultWrapper<Nothing>()
}

interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    fun getAuthStateFlow(): StateFlow<FirebaseUser?>

    suspend fun signInWithEmailPassword(email: String, password: String): AuthResultWrapper<FirebaseUser>
    suspend fun signUpWithEmailPassword(email: String, password: String, displayName: String? = null): AuthResultWrapper<FirebaseUser>
    suspend fun signInWithCredential(credential: AuthCredential): AuthResultWrapper<FirebaseUser> // For Google Sign-In etc.
    suspend fun signOut()
    suspend fun sendPasswordResetEmail(email: String): AuthResultWrapper<Unit>
    suspend fun getUserDocument(uid: String): AuthResultWrapper<User?>
}

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseModule.authInstance,
    private val db: FirebaseFirestore = FirebaseModule.firestoreInstance
) : AuthRepository {

    private val _authStateFlow = MutableStateFlow(auth.currentUser)

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authStateFlow.value = firebaseAuth.currentUser
        }
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override fun getAuthStateFlow(): StateFlow<FirebaseUser?> = _authStateFlow

    override suspend fun signInWithEmailPassword(email: String, password: String): AuthResultWrapper<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                // Optionally update last login time on Firestore
                db.collection("users").document(it.uid).update("lastLogin", Date()).await()
                AuthResultWrapper.Success(it)
            } ?: AuthResultWrapper.Error(Exception("Sign in failed: User is null"))
        } catch (e: Exception) {
            AuthResultWrapper.Error(e)
        }
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String, displayName: String?): AuthResultWrapper<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                // Create user document in Firestore
                val newUser = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = displayName ?: firebaseUser.displayName, // Use provided or default from Firebase
                    createdAt = Date(), // Will be overwritten by @ServerTimestamp if field exists
                    lastLogin = Date()  // Will be overwritten by @ServerTimestamp if field exists
                )
                db.collection("users").document(firebaseUser.uid).set(newUser).await()
                AuthResultWrapper.Success(firebaseUser)
            } ?: AuthResultWrapper.Error(Exception("Sign up failed: User is null after creation"))
        } catch (e: Exception) {
            AuthResultWrapper.Error(e)
        }
    }

    override suspend fun signInWithCredential(credential: AuthCredential): AuthResultWrapper<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            result.user?.let { firebaseUser ->
                // Check if this is a new user (e.g., first time Google Sign-In)
                val isNewUser = result.additionalUserInfo?.isNewUser ?: false
                if (isNewUser) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email,
                        displayName = firebaseUser.displayName,
                        createdAt = Date(),
                        lastLogin = Date()
                    )
                    db.collection("users").document(firebaseUser.uid).set(newUser).await()
                } else {
                    db.collection("users").document(firebaseUser.uid).update("lastLogin", Date()).await()
                }
                AuthResultWrapper.Success(firebaseUser)
            } ?: AuthResultWrapper.Error(Exception("Sign in with credential failed: User is null"))
        } catch (e: Exception) {
            AuthResultWrapper.Error(e)
        }
    }


    override suspend fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Handle potential errors during sign out, though usually straightforward
            throw e // Or log and absorb if non-critical
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResultWrapper<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResultWrapper.Success(Unit)
        } catch (e: Exception) {
            AuthResultWrapper.Error(e)
        }
    }

    override suspend fun getUserDocument(uid: String): AuthResultWrapper<User?> {
        return try {
            val documentSnapshot = db.collection("users").document(uid).get().await()
            val user = documentSnapshot.toObject(User::class.java)
            AuthResultWrapper.Success(user)
        } catch (e: Exception) {
            AuthResultWrapper.Error(e)
        }
    }
}
