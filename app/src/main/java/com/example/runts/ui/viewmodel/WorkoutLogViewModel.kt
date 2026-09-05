package com.example.runts.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runts.domain.usecase.RegisterWorkoutExecutionUseCase
import com.example.runts.domain.model.Workout
import com.example.runts.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class WorkoutLogUiState {
    object Idle : WorkoutLogUiState()
    object Loading : WorkoutLogUiState()
    object Saved : WorkoutLogUiState()
    data class Error(val message: String) : WorkoutLogUiState()
}

@HiltViewModel
class WorkoutLogViewModel @Inject constructor(
    private val registerWorkoutExecutionUseCase: RegisterWorkoutExecutionUseCase,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutLogUiState>(WorkoutLogUiState.Idle)
    val uiState: StateFlow<WorkoutLogUiState> = _uiState.asStateFlow()
    private val _prescribedWorkout = MutableStateFlow<Workout?>(null)
    val prescribedWorkout: StateFlow<Workout?> = _prescribedWorkout.asStateFlow()

    fun loadPrescription(workoutId: String) {
        viewModelScope.launch { _prescribedWorkout.value = workoutRepository.getWorkoutById(workoutId) }
    }

    fun saveExecution(
        prescribedWorkoutId: String?,
        athleteId: String,
        distanceKm: Double,
        durationSeconds: Int,
        pace: String,
        avgHr: Int?,
        pse: Int,
        comments: String?
    ) {
        viewModelScope.launch {
            _uiState.value = WorkoutLogUiState.Loading
            val result = registerWorkoutExecutionUseCase(
                prescribedWorkoutId = prescribedWorkoutId,
                athleteId = athleteId,
                executionDate = LocalDate.now().toString(),
                actualDistanceKm = distanceKm,
                actualDurationSeconds = durationSeconds,
                actualPace = pace,
                actualAvgHeartRate = avgHr,
                pse = pse,
                rawGpsDataJson = null,
                comments = comments
            )

            result.onSuccess {
                _uiState.value = WorkoutLogUiState.Saved
            }.onFailure { e ->
                _uiState.value = WorkoutLogUiState.Error(e.message ?: "Erro ao salvar treino")
            }
        }
    }
}
