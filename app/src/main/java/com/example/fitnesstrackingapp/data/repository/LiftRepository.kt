package com.example.fitnesstrackingapp.data.repository // Adjust package name as needed

import com.example.fitnesstrackingapp.data.firebase.FirebaseModule
import com.example.fitnesstrackingapp.data.model.UserLift
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// Using AuthResultWrapper for consistency, can be renamed to DataResultWrapper or similar
typealias DataResult<T> = AuthResultWrapper<T>

interface LiftRepository {
    suspend fun addLift(lift: UserLift): DataResult<String> // Returns ID of the new lift
    suspend fun getUserLifts(userId: String, exerciseId: String? = null): DataResult<List<UserLift>>
    suspend fun getLiftById(liftId: String): DataResult<UserLift?>
    suspend fun updateLift(lift: UserLift): DataResult<Unit>
    suspend fun deleteLift(liftId: String): DataResult<Unit>
}

class LiftRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseModule.firestoreInstance,
    private val auth: FirebaseAuth = FirebaseModule.authInstance // To get current user if needed
) : LiftRepository {

    private val liftsCollection = db.collection("userLifts")

    override suspend fun addLift(lift: UserLift): DataResult<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return DataResult.Error(Exception("User not authenticated to add lift."))
            }
            // Ensure the lift's userId matches the currently authenticated user
            val liftWithUserId = lift.copy(userId = currentUser.uid)

            val documentReference = liftsCollection.add(liftWithUserId).await()
            DataResult.Success(documentReference.id)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }

    override suspend fun getUserLifts(userId: String, exerciseId: String?): DataResult<List<UserLift>> {
        return try {
            var query: Query = liftsCollection.whereEqualTo("userId", userId)
            if (exerciseId != null) {
                query = query.whereEqualTo("exerciseId", exerciseId)
            }
            // Order by date, most recent first
            query = query.orderBy("date", Query.Direction.DESCENDING)

            val snapshot = query.get().await()
            val lifts = snapshot.toObjects(UserLift::class.java)
            DataResult.Success(lifts)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }

    override suspend fun getLiftById(liftId: String): DataResult<UserLift?> {
        return try {
            val documentSnapshot = liftsCollection.document(liftId).get().await()
            val lift = documentSnapshot.toObject(UserLift::class.java)
            DataResult.Success(lift)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }

    override suspend fun updateLift(lift: UserLift): DataResult<Unit> {
        return try {
            if (lift.id == null) {
                return DataResult.Error(IllegalArgumentException("Lift ID cannot be null for update."))
            }
            val currentUser = auth.currentUser
            if (currentUser == null || currentUser.uid != lift.userId) {
                return DataResult.Error(Exception("User not authorized to update this lift."))
            }
            liftsCollection.document(lift.id!!).set(lift, SetOptions.merge()).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }

    override suspend fun deleteLift(liftId: String): DataResult<Unit> {
        return try {
            // Optional: Before deleting, verify the lift belongs to the current user
            // val liftDoc = getLiftById(liftId)
            // if (liftDoc is DataResult.Success && liftDoc.data?.userId != auth.currentUser?.uid) {
            //    return DataResult.Error(Exception("User not authorized to delete this lift."))
            // }
            liftsCollection.document(liftId).delete().await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }
}
