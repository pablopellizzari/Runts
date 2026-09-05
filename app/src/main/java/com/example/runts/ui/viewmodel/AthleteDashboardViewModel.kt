package com.example.runts.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runts.domain.model.Workout
import com.example.runts.domain.model.User
import com.example.runts.domain.repository.UserRepository
import com.example.runts.domain.model.RaceEvent
import com.example.runts.domain.repository.RaceEventRepository
import com.example.runts.domain.repository.WorkoutRepository
import com.example.runts.domain.usecase.SyncWorkoutsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AthleteDashboardViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val syncWorkoutsUseCase: SyncWorkoutsUseCase,
    private val userRepository: UserRepository,
    private val raceEventRepository: RaceEventRepository
) : ViewModel() {

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    private val _races = MutableStateFlow<List<RaceEvent>>(emptyList())
    val races: StateFlow<List<RaceEvent>> = _races.asStateFlow()

    fun loadDashboard(athleteId: String) {
        viewModelScope.launch {
            launch {
                userRepository.getUserById(athleteId).collect { user -> _currentUser.value = user }
            }
            launch {
                raceEventRepository.getRaceEventsByAthlete(athleteId).collect { races -> _races.value = races }
            }
            syncWorkoutsUseCase(athleteId)

            workoutRepository.getWorkoutsByAthlete(athleteId).collect { list ->
                _workouts.value = list
            }
        }
    }
}
