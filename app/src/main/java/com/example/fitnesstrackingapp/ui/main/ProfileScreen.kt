package com.example.fitnesstrackingapp.ui.main // Adjust package name as needed

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackingapp.ui.auth.AuthViewModel
import com.example.fitnesstrackingapp.ui.theme.FitnessTrackingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel, // To get user info and handle sign out
    onSignedOut: () -> Unit
) {
    val currentUser by authViewModel.authState.collectAsState()

    // If user becomes null (e.g., due to external sign out or token expiry), trigger navigation
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            onSignedOut()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile & Settings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            currentUser?.let { user ->
                Text("User Profile", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Text("UID: ${user.uid}", style = MaterialTheme.typography.bodyLarge)
                user.email?.let { email ->
                    Text("Email: $email", style = MaterialTheme.typography.bodyLarge)
                }
                user.displayName?.let { displayName ->
                    Text("Display Name: $displayName", style = MaterialTheme.typography.bodyLarge)
                }
                // Add more user details or settings options here
                // e.g., Change Display Name, Change Password, Preferred Units
            } ?: run {
                Text("Not logged in.", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes button to bottom

            Button(
                onClick = {
                    authViewModel.signOut()
                    // `onSignedOut` will be triggered by the LaunchedEffect observing `currentUser`
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Sign Out")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    FitnessTrackingAppTheme {
        // For preview, you might pass a mock AuthViewModel that has a dummy user.
        ProfileScreen(
            authViewModel = viewModel(), // Dummy ViewModel in preview
            onSignedOut = {}
        )
    }
}
