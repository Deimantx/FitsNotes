package com.example.fitnesstrackingapp.ui.templates // Adjust package name as needed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackingapp.data.model.Exercise
import com.example.fitnesstrackingapp.data.model.PredefinedExercises
import com.example.fitnesstrackingapp.data.model.TemplateExercise
import com.example.fitnesstrackingapp.data.model.WorkoutTemplate
import com.example.fitnesstrackingapp.data.repository.AuthRepository
import com.example.fitnesstrackingapp.data.repository.AuthRepositoryImpl
import com.example.fitnesstrackingapp.data.repository.DataResult
import com.example.fitnesstrackingapp.data.repository.TemplateRepository
import com.example.fitnesstrackingapp.data.repository.TemplateRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class TemplatesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userTemplates: List<WorkoutTemplate> = emptyList(),
    val selectedTemplate: WorkoutTemplate? = null,
    val operationSuccess: Boolean = false, // For add/update/delete
    val predefinedExercises: List<Exercise> = PredefinedExercises.list
)

// class TemplatesViewModel(
//    private val templateRepository: TemplateRepository,
//    private val authRepository: AuthRepository
// ) : ViewModel() {
class TemplatesViewModel : ViewModel() { // Using direct instantiation for simplicity

    private val templateRepository: TemplateRepository = TemplateRepositoryImpl()
    private val authRepository: AuthRepository = AuthRepositoryImpl()

    private val _uiState = MutableStateFlow(TemplatesUiState())
    val uiState: StateFlow<TemplatesUiState> = _uiState.asStateFlow()

    fun loadUserTemplates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val currentUserId = authRepository.getCurrentUser()?.uid
            if (currentUserId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not authenticated.")
                return@launch
            }
            when (val result = templateRepository.getUserTemplates(currentUserId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, userTemplates = result.data)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.exception.message ?: "Failed to load templates.")
                }
            }
        }
    }

    fun loadTemplateById(templateId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedTemplate = null)
             when (val result = templateRepository.getTemplateById(templateId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, selectedTemplate = result.data)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.exception.message ?: "Failed to load template details.")
                }
            }
        }
    }


    fun addTemplate(name: String, description: String?, exercises: List<TemplateExercise>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, operationSuccess = false)
            val currentUserId = authRepository.getCurrentUser()?.uid
            if (currentUserId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not authenticated.")
                return@launch
            }
            val newTemplate = WorkoutTemplate(
                userId = currentUserId, // Will be set by repo if not passed
                name = name,
                description = description,
                exercises = exercises,
                createdAt = Date(), // Firestore @ServerTimestamp will override
                updatedAt = Date()  // Firestore @ServerTimestamp will override
            )
            when (val result = templateRepository.addTemplate(newTemplate)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, operationSuccess = true)
                    loadUserTemplates() // Refresh list
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.exception.message ?: "Failed to add template.")
                }
            }
        }
    }

    fun updateTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
             _uiState.value = _uiState.value.copy(isLoading = true, error = null, operationSuccess = false)
             val currentUserId = authRepository.getCurrentUser()?.uid
            if (currentUserId == null || template.userId != currentUserId) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not authorized.")
                return@launch
            }

            // Ensure updatedAt is handled by server or set appropriately
            val templateToUpdate = template.copy(updatedAt = null)


            when (val result = templateRepository.updateTemplate(templateToUpdate)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, operationSuccess = true, selectedTemplate = template) // update selected
                    // Potentially reload all templates if list view is visible and might need update
                    // loadUserTemplates()
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.exception.message ?: "Failed to update template.")
                }
            }
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, operationSuccess = false)
            // Optional: Verify ownership if not handled by repository rules
            when (val result = templateRepository.deleteTemplate(templateId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, operationSuccess = true)
                    loadUserTemplates() // Refresh list
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.exception.message ?: "Failed to delete template.")
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetOperationSuccessFlag() {
        _uiState.value = _uiState.value.copy(operationSuccess = false)
    }

    fun clearSelectedTemplate() {
        _uiState.value = _uiState.value.copy(selectedTemplate = null)
    }
}
