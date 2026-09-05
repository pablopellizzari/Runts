package com.example.runts.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runts.domain.model.RaceEvent
import com.example.runts.domain.model.RacePriority
import com.example.runts.domain.repository.RaceEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RaceCalendarViewModel @Inject constructor(
    private val raceEventRepository: RaceEventRepository
) : ViewModel() {

    private val _races = MutableStateFlow<List<RaceEvent>>(emptyList())
    val races: StateFlow<List<RaceEvent>> = _races.asStateFlow()
    private val _editingRace = MutableStateFlow<RaceEvent?>(null)
    val editingRace: StateFlow<RaceEvent?> = _editingRace.asStateFlow()

    fun loadRaces(athleteId: String) {
        viewModelScope.launch {
            raceEventRepository.getRaceEventsByAthlete(athleteId).collect { list ->
                _races.value = list
            }
        }
    }

    fun loadRace(raceId: String?) {
        if (raceId == null) {
            _editingRace.value = null
            return
        }
        viewModelScope.launch { _editingRace.value = raceEventRepository.getRaceById(raceId) }
    }

    fun saveRace(
        raceId: String? = null,
        athleteId: String,
        name: String,
        date: String,
        modality: String,
        targetTime: String?,
        priority: RacePriority,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val event = RaceEvent(
                id = raceId ?: "race_${java.util.UUID.randomUUID()}",
                athleteId = athleteId,
                name = name,
                date = date,
                modality = modality,
                targetTime = targetTime,
                priority = priority
            )
            onResult(raceEventRepository.saveRaceEvent(event))
        }
    }

    fun deleteRace(raceId: String, athleteId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch { onResult(raceEventRepository.deleteRaceEvent(raceId, athleteId)) }
    }
}
