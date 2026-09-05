package com.example.runts.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runts.domain.model.StravaConnectionStatus
import com.example.runts.domain.repository.ExternalIntegrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntegrationsUiState(
    val loading: Boolean = true,
    val strava: StravaConnectionStatus = StravaConnectionStatus(false),
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class IntegrationsViewModel @Inject constructor(
    private val repository: ExternalIntegrationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(IntegrationsUiState())
    val state: StateFlow<IntegrationsUiState> = _state.asStateFlow()

    private val _authorizationUrl = MutableStateFlow<String?>(null)
    val authorizationUrl: StateFlow<String?> = _authorizationUrl.asStateFlow()

    fun loadStatus() {
        viewModelScope.launch {
            repository.getStravaStatus().fold(
                onSuccess = { _state.value = _state.value.copy(loading = false, strava = it, error = null) },
                onFailure = { _state.value = _state.value.copy(loading = false, error = it.message) }
            )
        }
    }

    fun connectStrava() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, message = null)
            repository.getStravaAuthorizationUrl().fold(
                onSuccess = {
                    _authorizationUrl.value = it
                    _state.value = _state.value.copy(loading = false)
                },
                onFailure = { _state.value = _state.value.copy(loading = false, error = it.message) }
            )
        }
    }

    fun consumeAuthorizationUrl() { _authorizationUrl.value = null }

    fun syncStrava() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, message = null)
            repository.syncStrava().fold(
                onSuccess = {
                    _state.value = _state.value.copy(loading = false, message = "${it.imported} atividade(s) sincronizada(s); ${it.matched} treino(s) encontrado(s).")
                    loadStatus()
                },
                onFailure = { _state.value = _state.value.copy(loading = false, error = it.message) }
            )
        }
    }

    fun disconnectStrava() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, message = null)
            repository.disconnectStrava().fold(
                onSuccess = { _state.value = IntegrationsUiState(loading = false, message = "Conta Strava desconectada.") },
                onFailure = { _state.value = _state.value.copy(loading = false, error = it.message) }
            )
        }
    }
}
