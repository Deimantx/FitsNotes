package com.example.fitnesstrackingapp.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackingapp.data.model.Exercise
import com.example.fitnesstrackingapp.data.model.TemplateExercise
import com.example.fitnesstrackingapp.ui.theme.FitnessTrackingAppTheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTemplateScreen(
    templatesViewModel: TemplatesViewModel = viewModel(),
    onTemplateCreated: () -> Unit
) {
    val uiState by templatesViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var templateName by remember { mutableStateOf("") }
    var templateDescription by remember { mutableStateOf("") }
    var templateExercises by remember { mutableStateOf(listOf<TemplateExercise>()) }

    // State for the "Add Exercise" sub-form
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.operationSuccess) {
        if (uiState.operationSuccess) {
            android.widget.Toast.makeText(context, "Template created successfully!", android.widget.Toast.LENGTH_SHORT).show()
            templatesViewModel.resetOperationSuccessFlag()
            onTemplateCreated()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, "Error: $it", android.widget.Toast.LENGTH_LONG).show()
            templatesViewModel.clearError()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Create Workout Template") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddExerciseDialog = true }) {
                Icon(Icons.Filled.AddCircleOutline, "Add Exercise to Template")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                // .verticalScroll(rememberScrollState()) // Use LazyColumn for exercises instead
        ) {
            OutlinedTextField(
                value = templateName,
                onValueChange = { templateName = it },
                label = { Text("Template Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = templateDescription,
                onValueChange = { templateDescription = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Exercises:", style = MaterialTheme.typography.titleMedium)
            if (templateExercises.isEmpty()) {
                Text("No exercises added yet. Click the '+' button to add some.",
                    modifier = Modifier.padding(vertical = 16.dp))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) { // Use weight to fill available space
                    itemsIndexed(templateExercises, key = { _, item -> item.id }) { index, exercise ->
                        TemplateExerciseListItem(
                            exercise = exercise,
                            onDelete = {
                                templateExercises = templateExercises.filterNot { it.id == exercise.id }
                            }
                            // TODO: Add onEdit if needed
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (templateName.isBlank()) {
                        android.widget.Toast.makeText(context, "Template name cannot be empty.", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                     if (templateExercises.isEmpty()) {
                        android.widget.Toast.makeText(context, "Add at least one exercise.", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    templatesViewModel.addTemplate(templateName, templateDescription.ifBlank { null }, templateExercises)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Create Template")
            }
        }
    }

    if (showAddExerciseDialog) {
        AddExerciseToTemplateDialog(
            predefinedExercises = uiState.predefinedExercises,
            onDismiss = { showAddExerciseDialog = false },
            onAddExercise = { exercise ->
                templateExercises = templateExercises + exercise
                showAddExerciseDialog = false
            }
        )
    }
}

@Composable
fun TemplateExerciseListItem(exercise: TemplateExercise, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.exerciseName, style = MaterialTheme.typography.titleSmall)
                Text("Sets: ${exercise.targetSets}, Reps: ${exercise.targetReps}", style = MaterialTheme.typography.bodySmall)
                exercise.notes?.let { Text("Notes: $it", style = MaterialTheme.typography.bodySmall) }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove Exercise", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseToTemplateDialog(
    predefinedExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onAddExercise: (TemplateExercise) -> Unit
) {
    var selectedExercise by remember { mutableStateOf(predefinedExercises.firstOrNull()) }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") } // Can be like "8-12" or "10"
    var notes by remember { mutableStateOf("") }
    var exerciseExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exercise to Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = exerciseExpanded,
                    onExpandedChange = { exerciseExpanded = !exerciseExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedExercise?.name ?: "Select Exercise",
                        onValueChange = {}, readOnly = true, label = { Text("Exercise") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exerciseExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = exerciseExpanded, onDismissRequest = { exerciseExpanded = false }) {
                        predefinedExercises.forEach { exercise ->
                            DropdownMenuItem(
                                text = { Text(exercise.name) },
                                onClick = { selectedExercise = exercise; exerciseExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(value = sets, onValueChange = { sets = it.filter{c -> c.isDigit()} }, label = { Text("Target Sets") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Target Reps (e.g., 5 or 8-12)") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (Optional)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val currentExercise = selectedExercise
                if (currentExercise == null) {
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

                onAddExercise(
                    TemplateExercise(
                        id = UUID.randomUUID().toString(),
                        exerciseId = currentExercise.id,
                        exerciseName = currentExercise.name,
                        targetSets = setsValue,
                        targetReps = reps,
                        notes = notes.ifBlank { null }
                    )
                )
            }) { Text("Add") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}


@Preview(showBackground = true)
@Composable
fun CreateTemplateScreenPreview() {
    FitnessTrackingAppTheme {
        CreateTemplateScreen(onTemplateCreated = {})
    }
}
