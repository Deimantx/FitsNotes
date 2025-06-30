package com.example.fitnesstrackingapp.data.repository // Adjust package name as needed

import com.example.fitnesstrackingapp.data.firebase.FirebaseModule
import com.example.fitnesstrackingapp.data.model.WorkoutTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// Using DataResult (typealias for AuthResultWrapper) for consistency
interface TemplateRepository {
    suspend fun addTemplate(template: WorkoutTemplate): DataResult<String> // Returns ID
    suspend fun getUserTemplates(userId: String): DataResult<List<WorkoutTemplate>>
    suspend fun getTemplateById(templateId: String): DataResult<WorkoutTemplate?>
    suspend fun updateTemplate(template: WorkoutTemplate): DataResult<Unit>
    suspend fun deleteTemplate(templateId: String): DataResult<Unit>
    // Functions to manage exercises within a template might be here or directly in ViewModel
    // For simplicity, template updates will often involve replacing the whole exercises list.
}

class TemplateRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseModule.firestoreInstance,
    private val auth: FirebaseAuth = FirebaseModule.authInstance
) : TemplateRepository {

    private val templatesCollection = db.collection("workoutTemplates")

    override suspend fun addTemplate(template: WorkoutTemplate): DataResult<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return DataResult.Error(Exception("User not authenticated."))
            }
            val templateWithUserId = template.copy(userId = currentUser.uid)
            val documentReference = templatesCollection.add(templateWithUserId).await()
            DataResult.Success(documentReference.id)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }

    override suspend fun getUserTemplates(userId: String): DataResult<List<WorkoutTemplate>> {
        return try {
            val snapshot = templatesCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val templates = snapshot.toObjects(WorkoutTemplate::class.java)
            DataResult.Success(templates)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }

    override suspend fun getTemplateById(templateId: String): DataResult<WorkoutTemplate?> {
        return try {
            val documentSnapshot = templatesCollection.document(templateId).get().await()
            val template = documentSnapshot.toObject(WorkoutTemplate::class.java)
            DataResult.Success(template)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }

    override suspend fun updateTemplate(template: WorkoutTemplate): DataResult<Unit> {
        return try {
            if (template.id == null) {
                return DataResult.Error(IllegalArgumentException("Template ID cannot be null for update."))
            }
            val currentUser = auth.currentUser
            if (currentUser == null || currentUser.uid != template.userId) {
                 return DataResult.Error(Exception("User not authorized to update this template."))
            }
            // Ensure updatedAt is set, Firestore @ServerTimestamp handles this if field is present and set to null
            val templateToUpdate = template.copy(updatedAt = null) // Let server set timestamp
            templatesCollection.document(template.id!!).set(templateToUpdate, SetOptions.merge()).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }

    override suspend fun deleteTemplate(templateId: String): DataResult<Unit> {
        return try {
            // Optional: Verify ownership before deleting
            // val templateResult = getTemplateById(templateId)
            // if (templateResult is DataResult.Success && templateResult.data?.userId != auth.currentUser?.uid) {
            //    return DataResult.Error(Exception("User not authorized to delete this template."))
            // }
            templatesCollection.document(templateId).delete().await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }
}
