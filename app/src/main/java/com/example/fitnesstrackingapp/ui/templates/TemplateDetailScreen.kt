package com.example.fitnesstrackingapp.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackingapp.data.model.TemplateExercise
import com.example.fitnesstrackingapp.data.model.WorkoutTemplate
import com.example.fitnesstrackingapp.ui.theme.FitnessTrackingAppTheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    templateId: String,
    templatesViewModel: TemplatesViewModel = viewModel(),
    onNavigateBack: () -> Unit
    // onStartWorkoutFromTemplate: (templateId: String) -> Unit // Future feature
) {
    val uiState by templatesViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val selectedTemplate = uiState.selectedTemplate

    var showEditDetailsDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    // For editing an existing exercise within the template
    var editingExercise by remember { mutableStateOf<TemplateExercise?>(null) }


    LaunchedEffect(templateId) {
        templatesViewModel.loadTemplateById(templateId)
    }

    // Clear selected template when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            templatesViewModel.clearSelectedTemplate()
        }
    }

    LaunchedEffect(uiState.operationSuccess) {
        if (uiState.operationSuccess) {
            android.widget.Toast.makeText(context, "Operation successful!", android.widget.Toast.LENGTH_SHORT).show()
            templatesViewModel.resetOperationSuccessFlag()
            // Reload details if an update happened
            templatesViewModel.loadTemplateById(templateId)
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, "Error: $it", android.widget.Toast.LENGTH_LONG).show()
            templatesViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedTemplate?.name ?: "Template Details") },
                actions = {
                    IconButton(onClick = { showEditDetailsDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Template Details")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingExercise = null; showAddExerciseDialog = true }) {
                Icon(Icons.Filled.AddCircleOutline, "Add Exercise to Template")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading && selectedTemplate == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (selectedTemplate == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Template not found or error loading.")
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
                Text(selectedTemplate.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (!selectedTemplate.description.isNullOrBlank()) {
                    Text(selectedTemplate.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Exercises:", style = MaterialTheme.typography.titleMedium)
                if (selectedTemplate.exercises.isEmpty()) {
                    Text("No exercises in this template. Add some!", modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(selectedTemplate.exercises, key = { _, item -> item.id }) { index, exercise ->
                            TemplateExerciseDetailItem(
                                exercise = exercise,
                                onEdit = {
                                    editingExercise = exercise
                                    showAddExerciseDialog = true // Reuse dialog for editing
                                },
                                onDelete = {
                                    val updatedExercises = selectedTemplate.exercises.filterNot { it.id == exercise.id }
                                    templatesViewModel.updateTemplate(selectedTemplate.copy(exercises = updatedExercises))
                                }
                            )
                        }
                    }
                }
                // Button(onClick = { /* onStartWorkoutFromTemplate(templateId) */ }, modifier = Modifier.fillMaxWidth().padding(vertical=16.dp)) {
                // Text("Start Workout with this Template")
                // }
            }
        }
    }

    if (showEditDetailsDialog && selectedTemplate != null) {
        EditTemplateDetailsDialog(
            template = selectedTemplate,
            onDismiss = { showEditDetailsDialog = false },
            onSave = { updatedName, updatedDescription ->
                templatesViewModel.updateTemplate(
                    selectedTemplate.copy(name = updatedName, description = updatedDescription)
                )
                showEditDetailsDialog = false
            }
        )
    }

    if (showAddExerciseDialog && selectedTemplate != null) {
        // If editingExercise is not null, it means we are editing, otherwise adding new
        val exerciseToEdit = editingExercise
        AddExerciseToTemplateDialog( // Re-use the dialog from CreateTemplateScreen, or a similar one
            predefinedExercises = uiState.predefinedExercises,
            initialExercise = exerciseToEdit, // Pass current exercise if editing
            onDismiss = { showAddExerciseDialog = false; editingExercise = null; },
            onAddExercise = { newExercise -> // This lambda is for adding a NEW exercise
                val updatedExercises = selectedTemplate.exercises + newExercise.copy(id = UUID.randomUUID().toString()) // Ensure new ID
                templatesViewModel.updateTemplate(selectedTemplate.copy(exercises = updatedExercises))
                showAddExerciseDialog = false
            },
            onUpdateExercise = { updatedExercise -> // This lambda is for UPDATING an existing exercise
                 val updatedExercises = selectedTemplate.exercises.map {
                    if (it.id == updatedExercise.id) updatedExercise else it
                }
                templatesViewModel.updateTemplate(selectedTemplate.copy(exercises = updatedExercises))
                showAddExerciseDialog = false
                editingExercise = null
            }
        )
    }
}

@Composable
fun TemplateExerciseDetailItem(
    exercise: TemplateExercise,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.exerciseName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Sets: ${exercise.targetSets}, Reps: ${exercise.targetReps}", style = MaterialTheme.typography.bodyMedium)
                exercise.notes?.let { Text("Notes: $it", style = MaterialTheme.typography.bodySmall) }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Exercise", tint = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove Exercise", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTemplateDetailsDialog(
    template: WorkoutTemplate,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?) -> Unit
) {
    var name by remember { mutableStateOf(template.name) }
    var description by remember { mutableStateOf(template.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Template Details") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, description.ifBlank { null }) }) { Text("Save") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}


// Re-using or adapting AddExerciseToTemplateDialog from CreateTemplateScreen for editing
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseToTemplateDialog( // This dialog is now overloaded for add and edit
    predefinedExercises: List<Exercise>,
    initialExercise: TemplateExercise? = null, // If null, it's for adding new. If provided, for editing.
    onDismiss: () -> Unit,
    onAddExercise: (TemplateExercise) -> Unit, // Called when adding a new exercise
    onUpdateExercise: (TemplateExercise) -> Unit // Called when updating an existing exercise
) {
    var selectedExercise by remember { mutableStateOf(initialExercise?.let { PredefinedExercises.findByName(it.exerciseName) } ?: predefinedExercises.firstOrNull()) }
    var sets by remember { mutableStateOf(initialExercise?.targetSets?.toString() ?: "") }
    var reps by remember { mutableStateOf(initialExercise?.targetReps ?: "") }
    var notes by remember { mutableStateOf(initialExercise?.notes ?: "") }
    var exerciseExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isEditing = initialExercise != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Exercise" else "Add Exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = exerciseExpanded, onExpandedChange = { exerciseExpanded = !exerciseExpanded }) {
                    OutlinedTextField(
                        value = selectedExercise?.name ?: "Select Exercise",
                        onValueChange = {}, readOnly = true, label = { Text("Exercise") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exerciseExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = exerciseExpanded, onDismissRequest = { exerciseExpanded = false }) {
                        predefinedExercises.forEach { exercise ->
                            DropdownMenuItem(text = { Text(exercise.name) }, onClick = { selectedExercise = exercise; exerciseExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = sets, onValueChange = { sets = it.filter{c -> c.isDigit()} }, label = { Text("Target Sets") })
                OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Target Reps (e.g., 5 or 8-12)") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (Optional)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val currentExerciseDef = selectedExercise
                if (currentExerciseDef == null) {
                    android.widget.Toast.makeText(context, "Please select an exercise.", android.widget.Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val setsValue = sets.toIntOrNull()
                if (setsValue == null || setsValue <= 0) {
                     android.widget.Toast.makeText(context, "Sets must be a positive number.", android.widget.Toast.LENGTH_SHORT).show()
                    return@Button
                }
                 if (reps.isBlank()) {
                     android.widget.Toast.makeText(context, "Reps cannot be empty.", android.widget.Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val exerciseData = TemplateExercise(
                    id = initialExercise?.id ?: UUID.randomUUID().toString(), // Keep original ID if editing
                    exerciseId = currentExerciseDef.id,
                    exerciseName = currentExerciseDef.name,
                    targetSets = setsValue,
                    targetReps = reps,
                    notes = notes.ifBlank { null },
                    order = initialExercise?.order ?: 0 // Preserve order or default
                )
                if (isEditing) {
                    onUpdateExercise(exerciseData)
                } else {
                    onAddExercise(exerciseData)
                }
            }) { Text(if (isEditing) "Update" else "Add") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}


@Preview(showBackground = true)
@Composable
fun TemplateDetailScreenPreview() {
    FitnessTrackingAppTheme {
        // This preview is hard to set up without a running ViewModel and templateId
        // You'd typically pass a mock ViewModel state for a meaningful preview.
        Text("Template Detail Screen Preview (Needs ViewModel)")
        // TemplateDetailScreen(templateId = "previewId", onNavigateBack = {})
    }
}
