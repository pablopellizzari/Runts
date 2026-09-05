package com.example.runts.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runts.domain.model.AthleteStats
import com.example.runts.domain.usecase.CalculateAthleteStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AthleteStatsViewModel @Inject constructor(
    private val calculateAthleteStatsUseCase: CalculateAthleteStatsUseCase
) : ViewModel() {

    private val _stats = MutableStateFlow<AthleteStats?>(null)
    val stats: StateFlow<AthleteStats?> = _stats.asStateFlow()

    fun loadStats(athleteId: String) {
        viewModelScope.launch {
            calculateAthleteStatsUseCase(athleteId).collect {
                _stats.value = it
            }
        }
    }
}
