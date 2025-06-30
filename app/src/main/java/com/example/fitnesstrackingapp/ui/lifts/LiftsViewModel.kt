package com.example.fitnesstrackingapp.ui.lifts // Adjust package name as needed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackingapp.data.model.PredefinedExercises
import com.example.fitnesstrackingapp.data.model.UserLift
import com.example.fitnesstrackingapp.data.model.Exercise
import com.example.fitnesstrackingapp.data.repository.AuthRepository
import com.example.fitnesstrackingapp.data.repository.AuthRepositoryImpl
import com.example.fitnesstrackingapp.data.repository.DataResult
import com.example.fitnesstrackingapp.data.repository.LiftRepository
import com.example.fitnesstrackingapp.data.repository.LiftRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class LiftsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userLifts: List<UserLift> = emptyList(),
    val selectedLift: UserLift? = null,
    val operationSuccess: Boolean = false, // For add/update/delete success
    val predefinedExercises: List<Exercise> = PredefinedExercises.list
)

// class LiftsViewModel(
//     private val liftRepository: LiftRepository,
//     private val authRepository: AuthRepository
// ) : ViewModel() {
class LiftsViewModel : ViewModel() { // Using direct instantiation for simplicity

    private val liftRepository: LiftRepository = LiftRepositoryImpl()
    private val authRepository: AuthRepository = AuthRepositoryImpl() // To get current user ID

    private val _uiState = MutableStateFlow(LiftsUiState())
    val uiState: StateFlow<LiftsUiState> = _uiState.asStateFlow()

    fun loadUserLifts(exerciseId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val currentUserId = authRepository.getCurrentUser()?.uid
            if (currentUserId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not authenticated.")
                return@launch
            }

            when (val result = liftRepository.getUserLifts(currentUserId, exerciseId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, userLifts = result.data)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.exception.message ?: "Failed to load lifts.")
                }
            }
        }
    }

    fun addLift(
        exerciseId: String,
        exerciseName: String,
        date: Date,
        weight: Double,
        reps: Int,
        sets: Int,
        notes: String?,
        isPr: Boolean,
        unit: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, operationSuccess = false)
            val currentUserId = authRepository.getCurrentUser()?.uid
            if (currentUserId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not authenticated.")
                return@launch
            }

            val newLift = UserLift(
                userId = currentUserId, // Will be set by repository if not passed
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                date = date,
                weight = weight,
                reps = reps,
                sets = sets,
                notes = notes,
                isPr = isPr,
                unit = unit
                // createdAt will be set by server
            )

            when (val result = liftRepository.addLift(newLift)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, operationSuccess = true)
                    // Optionally reload lifts or add to current list to update UI immediately
                    loadUserLifts()
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.exception.message ?: "Failed to add lift.")
                }
            }
        }
    }

    fun deleteLift(liftId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, operationSuccess = false)
            when (val result = liftRepository.deleteLift(liftId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, operationSuccess = true)
                    // Reload lifts to reflect deletion
                    loadUserLifts()
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.exception.message ?: "Failed to delete lift.")
                }
            }
        }
    }

    // TODO: Implement updateLift if needed

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetOperationSuccessFlag() {
        _uiState.value = _uiState.value.copy(operationSuccess = false)
    }
}
