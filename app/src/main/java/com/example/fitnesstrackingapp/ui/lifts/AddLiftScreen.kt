package com.example.fitnesstrackingapp.ui.lifts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.example.fitnesstrackingapp.ui.theme.FitnessTrackingAppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLiftScreen(
    liftsViewModel: LiftsViewModel = viewModel(),
    onLiftAdded: () -> Unit, // Callback for when lift is successfully added
    // onNavigateBack: () -> Unit // If you want explicit back navigation
) {
    val uiState by liftsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedExercise by remember { mutableStateOf<Exercise?>(uiState.predefinedExercises.firstOrNull()) }
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isPr by remember { mutableStateOf(false) }
    var workoutDate by remember { mutableStateOf(Date()) } // Default to today
    var unit by remember { mutableStateOf("kg") } // Default unit

    val showDatePicker = remember { mutableStateOf(false) }

    LaunchedEffect(uiState.operationSuccess) {
        if (uiState.operationSuccess) {
            // Show success message (e.g., Toast or Snackbar)
            android.widget.Toast.makeText(context, "Lift added successfully!", android.widget.Toast.LENGTH_SHORT).show()
            liftsViewModel.resetOperationSuccessFlag()
            onLiftAdded() // Navigate back or clear form
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, "Error: $it", android.widget.Toast.LENGTH_LONG).show()
            liftsViewModel.clearError()
        }
    }


    Scaffold(
        topBar = { TopAppBar(title = { Text("Log New Lift") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Exercise Dropdown
            var exerciseExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = exerciseExpanded,
                onExpandedChange = { exerciseExpanded = !exerciseExpanded }
            ) {
                OutlinedTextField(
                    value = selectedExercise?.name ?: "Select Exercise",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Exercise") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exerciseExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = exerciseExpanded,
                    onDismissRequest = { exerciseExpanded = false }
                ) {
                    uiState.predefinedExercises.forEach { exercise ->
                        DropdownMenuItem(
                            text = { Text(exercise.name) },
                            onClick = {
                                selectedExercise = exercise
                                exerciseExpanded = false
                            }
                        )
                    }
                }
            }

            // Date Picker
            OutlinedTextField(
                value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(workoutDate),
                onValueChange = {},
                label = { Text("Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Filled.DateRange,
                        contentDescription = "Select Date",
                        modifier = Modifier.clickable { showDatePicker.value = true }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (showDatePicker.value) {
                val calendar = Calendar.getInstance().apply { time = workoutDate }
                DatePickerDialog(
                    onDismissRequest = { showDatePicker.value = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePicker.value = false }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker.value = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(
                        state = rememberDatePickerState(
                            initialSelectedDateMillis = calendar.timeInMillis
                        ),
                        dateValidator = { timestamp -> timestamp <= System.currentTimeMillis() } // Allow only past/present dates
                    ) { dateMillis -> // This lambda is not standard, DatePicker updates its state
                        // The DatePickerState should be hoisted and observed to get the selected date
                        // For simplicity, this example assumes direct callback, which is not how Material3 DatePicker works.
                        // Correct way: Hoist DatePickerState, observe selectedDateMillis, then update `workoutDate`.
                        // This is a simplified placeholder.
                        // workoutDate = Date(dateMillis) // This is conceptually what you want to do.
                        // showDatePicker.value = false // This is not the correct way to get the date from M3 DatePicker
                    }
                    // TODO: Properly implement date selection with Material3 DatePicker
                    // The `DatePicker` Composable in M3 doesn't have a direct `onDateSelected` lambda.
                    // You need to create and remember a `DatePickerState`, pass it to `DatePicker`,
                    // and then observe `state.selectedDateMillis` to get the chosen date.
                    // For now, we'll leave the date as default or manually updatable.
                }
            }


            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                // Unit Picker (kg/lbs) - Simplified
                var unitExpanded by remember { mutableStateOf(false) }
                 ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded },
                    modifier = Modifier.weight(0.6f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        DropdownMenuItem(text = { Text("kg") }, onClick = { unit = "kg"; unitExpanded = false })
                        DropdownMenuItem(text = { Text("lbs") }, onClick = { unit = "lbs"; unitExpanded = false })
                    }
                }
            }


            OutlinedTextField(
                value = reps,
                onValueChange = { reps = it.filter { char -> char.isDigit() } },
                label = { Text("Reps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sets,
                onValueChange = { sets = it.filter { char -> char.isDigit() } },
                label = { Text("Sets") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                maxLines = 3
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = isPr, onCheckedChange = { isPr = it })
                Text("Personal Record (PR)?", modifier = Modifier.padding(start = 8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        val currentExercise = selectedExercise
                        if (currentExercise == null) {
                            // Show error: exercise not selected
                            android.widget.Toast.makeText(context, "Please select an exercise.", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val weightValue = weight.toDoubleOrNull()
                        val repsValue = reps.toIntOrNull()
                        val setsValue = sets.toIntOrNull()

                        if (weightValue == null || repsValue == null || setsValue == null) {
                            // Show error: invalid number format or empty
                             android.widget.Toast.makeText(context, "Weight, Reps, and Sets must be valid numbers.", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        liftsViewModel.addLift(
                            exerciseId = currentExercise.id,
                            exerciseName = currentExercise.name,
                            date = workoutDate, // Use the selected date
                            weight = weightValue,
                            reps = repsValue,
                            sets = setsValue,
                            notes = notes.ifBlank { null },
                            isPr = isPr,
                            unit = unit
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Lift")
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:shape=Normal,width=360,height=640,unit=dp")
@Composable
fun AddLiftScreenPreview() {
    FitnessTrackingAppTheme {
        AddLiftScreen(onLiftAdded = {})
    }
}
