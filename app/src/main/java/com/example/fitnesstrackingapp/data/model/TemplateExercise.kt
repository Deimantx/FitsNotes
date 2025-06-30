package com.example.fitnesstrackingapp.data.model // Adjust package name as needed

import java.util.UUID

/**
 * Represents an exercise within a workout template.
 *
 * @property id A unique identifier for this specific entry within the template's exercise list.
 *              This is useful for updates/deletions within the list.
 * @property exerciseId The ID of the predefined exercise (links to Exercise.id).
 * @property exerciseName The name of the exercise (denormalized for easy display).
 * @property targetSets The target number of sets for this exercise in the template.
 * @property targetReps The target number of repetitions (can be a range, e.g., "8-12", or a single number).
 * @property notes Optional notes specific to this exercise within this template.
 * @property order The order of this exercise within the template (optional, for sorting).
 */
data class TemplateExercise(
    val id: String = UUID.randomUUID().toString(), // Client-generated unique ID for list management
    val exerciseId: String = "", // From PredefinedExercises
    val exerciseName: String = "", // Denormalized
    val targetSets: Int = 0,
    val targetReps: String = "", // e.g., "5" or "8-12"
    val notes: String? = null,
    val order: Int = 0 // For ordering exercises within a template
) {
    // No-argument constructor for Firestore deserialization if needed,
    // though typically this is part of a list within WorkoutTemplate.
    // If WorkoutTemplate.exercises is a List<TemplateExercise>, Firestore can handle it.
    constructor() : this(UUID.randomUUID().toString(), "", "", 0, "", null, 0)
}
