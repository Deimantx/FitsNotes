package com.example.fitnesstrackingapp.ui.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackingapp.data.model.WorkoutTemplate
import com.example.fitnesstrackingapp.ui.theme.FitnessTrackingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTemplatesScreen(
    templatesViewModel: TemplatesViewModel = viewModel(),
    onNavigateToCreateTemplate: () -> Unit,
    onNavigateToTemplateDetail: (templateId: String) -> Unit
) {
    val uiState by templatesViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        templatesViewModel.loadUserTemplates()
    }

    LaunchedEffect(uiState.operationSuccess) {
        if (uiState.operationSuccess && uiState.error == null) { // Avoid double toast if error also shown
            // templatesViewModel.loadUserTemplates() // Data is reloaded in ViewModel
            templatesViewModel.resetOperationSuccessFlag()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, "Error: $it", android.widget.Toast.LENGTH_LONG).show()
            templatesViewModel.clearError()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Workout Templates") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateTemplate) {
                Icon(Icons.Filled.Add, contentDescription = "Create New Template")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading && uiState.userTemplates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.userTemplates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No workout templates yet. Create your first one!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.userTemplates, key = { template -> template.id ?: template.hashCode() }) { template ->
                        WorkoutTemplateListItem(
                            template = template,
                            onViewDetails = { template.id?.let { onNavigateToTemplateDetail(it) } },
                            onDelete = {
                                template.id?.let {
                                    // Confirmation Dialog
                                    // For simplicity, directly calling delete. Add dialog in real app.
                                    templatesViewModel.deleteTemplate(it)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutTemplateListItem(
    template: WorkoutTemplate,
    onViewDetails: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails), // Make the whole card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (!template.description.isNullOrBlank()) {
                    Text(template.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
                Text(
                    "${template.exercises.size} exercise(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Template", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ViewTemplatesScreenPreview() {
    FitnessTrackingAppTheme {
        ViewTemplatesScreen(
            onNavigateToCreateTemplate = {},
            onNavigateToTemplateDetail = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutTemplateListItemPreview() {
    FitnessTrackingAppTheme {
        WorkoutTemplateListItem(
            template = WorkoutTemplate(
                id = "1",
                userId = "user1",
                name = "Full Body Strength A",
                description = "A comprehensive full body workout focusing on compound lifts.",
                exercises = listOf(
                    com.example.fitnesstrackingapp.data.model.TemplateExercise(exerciseName = "Squat", targetSets = 3, targetReps = "5"),
                    com.example.fitnesstrackingapp.data.model.TemplateExercise(exerciseName = "Bench Press", targetSets = 3, targetReps = "5")
                )
            ),
            onViewDetails = {},
            onDelete = {}
        )
    }
}
