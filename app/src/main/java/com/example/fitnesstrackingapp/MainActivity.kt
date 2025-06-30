package com.example.fitnesstrackingapp // Replace with your actual package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider // Required for AuthViewModel if not using Hilt
import com.example.fitnesstrackingapp.navigation.AppNavigation
import com.example.fitnesstrackingapp.ui.auth.AuthViewModel
import com.example.fitnesstrackingapp.ui.theme.FitnessTrackingAppTheme

class MainActivity : ComponentActivity() {

    // If not using Hilt, you might initialize ViewModel here or use viewModel() delegate in Composables
    // private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java) // Example without Hilt

        setContent {
            FitnessTrackingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the AuthViewModel instance if needed by AppNavigation directly,
                    // or let Composables get it via viewModel() delegate.
                    // For a shared ViewModel across different navigation graphs,
                    // it's common to create it at a higher level (like here or in AppNavigation itself)
                    // or use Hilt for proper scoping.
                    // The current AppNavigation uses viewModel() delegate by default.
                    AppNavigation()
                }
            }
        }
    }
}

// The Greeting Composable and its Preview can be removed or kept for scratch testing.
// @Composable
// fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name! Native Android version with Navigation.",
//        modifier = modifier
//    )
// }

// @Preview(showBackground = true)
// @Composable
// fun GreetingPreview() {
//    FitnessTrackingAppTheme {
//        Greeting("Android")
//    }
// }
