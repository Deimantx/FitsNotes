package com.example.fitnesstrackingapp.ui.main // Adjust package name as needed

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt // For Templates
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackingapp.ui.auth.AuthViewModel
import com.example.fitnesstrackingapp.ui.theme.FitnessTrackingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToAddLift: () -> Unit,
    onNavigateToLiftHistory: () -> Unit,
    onNavigateToViewTemplates: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fitness Home") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)
        ) {
            Text(
                "Welcome, ${authViewModel.getCurrentUser()?.displayName ?: authViewModel.getCurrentUser()?.email ?: "User"}!",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HomeCardButton(
                text = "Log New Lift",
                icon = Icons.Filled.FitnessCenter,
                onClick = onNavigateToAddLift
            )

            HomeCardButton(
                text = "View Lift History",
                icon = Icons.Filled.History,
                onClick = onNavigateToLiftHistory
            )

            HomeCardButton(
                text = "Workout Templates",
                icon = Icons.Filled.ListAlt,
                onClick = onNavigateToViewTemplates
            )

            // Add more cards or dashboard elements here
        }
    }
}

@Composable
fun HomeCardButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(32.dp))
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FitnessTrackingAppTheme {
        HomeScreen(
            authViewModel = viewModel(),
            onNavigateToProfile = {},
            onNavigateToAddLift = {},
            onNavigateToLiftHistory = {},
            onNavigateToViewTemplates = {}
        )
    }
}
