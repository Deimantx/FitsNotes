package com.example.fitnesstrackingapp.data.model // Adjust package name as needed

/**
 * Represents a predefined exercise.
 * This could be stored locally or fetched from a 'exercises' collection in Firestore.
 *
 * @property id A unique identifier for the exercise.
 * @property name The name of the exercise (e.g., "Bench Press", "Squat").
 * @property description An optional description of the exercise.
 * @property muscleGroup The primary muscle group targeted (e.g., "Chest", "Legs", "Back").
 * @property category E.g., "Barbell", "Dumbbell", "Bodyweight", "Machine".
 */
data class Exercise(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val muscleGroup: String? = null,
    val category: String? = null
    // val videoUrl: String? = null, // Optional: URL to an instruction video
    // val equipmentNeeded: List<String>? = null // Optional: List of equipment
) {
    // No-argument constructor for Firestore deserialization if fetched from there
    constructor() : this("", "", null, null, null)
}

// Simple list of predefined exercises (could be expanded or moved to a service/repository)
object PredefinedExercises {
    val list = listOf(
        Exercise(id = "bench_press", name = "Bench Press", muscleGroup = "Chest", category = "Barbell"),
        Exercise(id = "squat", name = "Squat", muscleGroup = "Legs", category = "Barbell"),
        Exercise(id = "deadlift", name = "Deadlift", muscleGroup = "Back/Legs", category = "Barbell"),
        Exercise(id = "overhead_press", name = "Overhead Press", muscleGroup = "Shoulders", category = "Barbell"),
        Exercise(id = "barbell_row", name = "Barbell Row", muscleGroup = "Back", category = "Barbell"),
        Exercise(id = "pull_ups", name = "Pull Ups", muscleGroup = "Back/Biceps", category = "Bodyweight"),
        Exercise(id = "push_ups", name = "Push Ups", muscleGroup = "Chest/Shoulders", category = "Bodyweight"),
        Exercise(id = "bicep_curls_db", name = "Dumbbell Bicep Curls", muscleGroup = "Biceps", category = "Dumbbell"),
        Exercise(id = "tricep_dips", name = "Tricep Dips", muscleGroup = "Triceps", category = "Bodyweight/Machine"),
        Exercise(id = "leg_press", name = "Leg Press", muscleGroup = "Legs", category = "Machine"),
        Exercise(id = "lat_pulldown", name = "Lat Pulldown", muscleGroup = "Back", category = "Machine"),
        Exercise(id = "dumbbell_shoulder_press", name = "Dumbbell Shoulder Press", muscleGroup = "Shoulders", category = "Dumbbell"),
        Exercise(id = "dumbbell_flyes", name = "Dumbbell Flyes", muscleGroup = "Chest", category = "Dumbbell"),
        Exercise(id = "leg_curls", name = "Leg Curls", muscleGroup = "Hamstrings", category = "Machine"),
        Exercise(id = "leg_extensions", name = "Leg Extensions", muscleGroup = "Quads", category = "Machine"),
        Exercise(id = "calf_raises", name = "Calf Raises", muscleGroup = "Calves", category = "Bodyweight/Machine")
    )

    fun findById(id: String?): Exercise? = list.find { it.id == id }
    fun findByName(name: String?): Exercise? = list.find { it.name == name }
}
