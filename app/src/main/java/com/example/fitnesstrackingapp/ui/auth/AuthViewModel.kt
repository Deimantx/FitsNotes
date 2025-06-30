package com.example.fitnesstrackingapp.ui.auth // Adjust package name as needed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackingapp.data.repository.AuthRepository
import com.example.fitnesstrackingapp.data.repository.AuthRepositoryImpl // Concrete implementation
import com.example.fitnesstrackingapp.data.repository.AuthResultWrapper
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Represents the state of an authentication UI screen.
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: FirebaseUser? = null, // Could also be your custom User model
    val isSuccess: Boolean = false // Generic success flag for operations like password reset
)

// In a real app with DI (Hilt/Koin), repository would be injected.
// class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
class AuthViewModel : ViewModel() {

    // For simplicity, instantiating directly. Use DI in a real app.
    private val authRepository: AuthRepository = AuthRepositoryImpl()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Exposes the auth state from the repository directly
    val authState: StateFlow<FirebaseUser?> = authRepository.getAuthStateFlow()


    init {
        // Initialize with current user state if needed, though authStateFlow handles this.
        // _uiState.value = _uiState.value.copy(currentUser = authRepository.getCurrentUser())
    }

    fun signInWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signInWithEmailPassword(email, password)) {
                is AuthResultWrapper.Success -> {
                    _uiState.value = AuthUiState(currentUser = result.data, isSuccess = true)
                }
                is AuthResultWrapper.Error -> {
                    _uiState.value = AuthUiState(error = result.exception.message ?: "Sign in failed")
                }
            }
        }
    }

    fun signUpWithEmailPassword(email: String, password: String, displayName: String? = null) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signUpWithEmailPassword(email, password, displayName)) {
                is AuthResultWrapper.Success -> {
                    _uiState.value = AuthUiState(currentUser = result.data, isSuccess = true)
                }
                is AuthResultWrapper.Error -> {
                    _uiState.value = AuthUiState(error = result.exception.message ?: "Sign up failed")
                }
            }
        }
    }

    fun signInWithCredential(credential: AuthCredential) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signInWithCredential(credential)) {
                is AuthResultWrapper.Success -> {
                    _uiState.value = AuthUiState(currentUser = result.data, isSuccess = true)
                }
                is AuthResultWrapper.Error -> {
                    _uiState.value = AuthUiState(error = result.exception.message ?: "Credential sign in failed")
                }
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is AuthResultWrapper.Success -> {
                    _uiState.value = AuthUiState(isSuccess = true) // Indicate success
                }
                is AuthResultWrapper.Error -> {
                    _uiState.value = AuthUiState(error = result.exception.message ?: "Password reset failed")
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            // UI state during sign out might not be critical unless there's a loading indicator
            authRepository.signOut()
            _uiState.value = AuthUiState(currentUser = null, isSuccess = false) // Reset state
        }
    }

    /**
     * Clears any error message from the UI state.
     * Useful after an error has been shown to the user.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Resets the isSuccess flag, useful after a successful one-off operation.
     */
    fun resetSuccessFlag() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}
