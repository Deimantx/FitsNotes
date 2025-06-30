package com.example.fitnesstrackingapp.ui.lifts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit // For future use
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackingapp.data.model.UserLift
import com.example.fitnesstrackingapp.ui.theme.FitnessTrackingAppTheme
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiftHistoryScreen(
    liftsViewModel: LiftsViewModel = viewModel(),
    onNavigateToAddLift: () -> Unit
    // onNavigateToEditLift: (liftId: String) -> Unit // For future use
) {
    val uiState by liftsViewModel.uiState.collectAsState()

    // Load lifts when the screen is first composed or when a relevant filter changes
    LaunchedEffect(Unit) { // Add filter keys here if you implement filtering
        liftsViewModel.loadUserLifts()
    }

    LaunchedEffect(uiState.operationSuccess) {
        if (uiState.operationSuccess && uiState.error == null) { // check error is null to avoid double toast for delete success
            // liftsViewModel.loadUserLifts() // Data is reloaded in ViewModel after successful operation
            liftsViewModel.resetOperationSuccessFlag()
        }
    }
     LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // Show toast or snackbar for error
            // For now, simple print
            println("LiftHistoryScreen Error: $it")
            liftsViewModel.clearError()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lift History") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddLift) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Lift")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.userLifts.isEmpty()) { // Show loading only if list is empty
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.userLifts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No lifts recorded yet. Add your first one!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.userLifts, key = { lift -> lift.id ?: lift.hashCode() }) { lift ->
                        LiftHistoryItem(
                            lift = lift,
                            onDelete = {
                                lift.id?.let { liftsViewModel.deleteLift(it) }
                            }
                            // onEdit = { lift.id?.let { onNavigateToEditLift(it) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiftHistoryItem(
    lift: UserLift,
    onDelete: () -> Unit
    // onEdit: () -> Unit // For future use
) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lift.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                lift.date?.let {
                    Text(
                        text = dateFormat.format(it),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Weight: ${lift.weight} ${lift.unit}", style = MaterialTheme.typography.bodyMedium)
            Text("Reps: ${lift.reps}", style = MaterialTheme.typography.bodyMedium)
            Text("Sets: ${lift.sets}", style = MaterialTheme.typography.bodyMedium)

            if (!lift.notes.isNullOrBlank()) {
                Text("Notes: ${lift.notes}", style = MaterialTheme.typography.bodySmall)
            }
            if (lift.isPr) {
                Text("🎉 Personal Record!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // IconButton(onClick = onEdit) {
                //     Icon(Icons.Filled.Edit, contentDescription = "Edit Lift", tint = MaterialTheme.colorScheme.secondary)
                // }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Lift", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LiftHistoryScreenPreview() {
    FitnessTrackingAppTheme {
        // Provide a mock LiftsViewModel with some data for preview if needed
        LiftHistoryScreen(
            liftsViewModel = viewModel(), // Dummy ViewModel
            onNavigateToAddLift = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LiftHistoryItemPreview() {
    FitnessTrackingAppTheme {
        LiftHistoryItem(
            lift = UserLift(
                id = "1",
                userId = "user1",
                exerciseId = "bench_press",
                exerciseName = "Bench Press",
                date = Date(),
                weight = 100.0,
                reps = 5,
                sets = 3,
                notes = "Felt strong today.",
                isPr = true,
                unit = "kg",
                createdAt = Date()
            ),
            onDelete = {}
            // onEdit = {}
        )
    }
}
