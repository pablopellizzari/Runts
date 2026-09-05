package com.example.runts.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runts.data.security.EncryptedStorageManager
import com.example.runts.domain.model.User
import com.example.runts.domain.model.UserType
import com.example.runts.domain.repository.UserRepository
import com.example.runts.domain.usecase.LinkAthleteToCoachUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Authenticated(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val linkAthleteToCoachUseCase: LinkAthleteToCoachUseCase,
    private val encryptedStorageManager: EncryptedStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _selectedRole = MutableStateFlow<UserType?>(UserType.ATHLETE)
    val selectedRole: StateFlow<UserType?> = _selectedRole.asStateFlow()

    fun selectRole(role: UserType) {
        _selectedRole.value = role
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = userRepository.loginUser(email, pass)

                result.onSuccess { user ->
                    encryptedStorageManager.saveAuthToken("jwt_token_${user.id}")
                    _uiState.value = AuthUiState.Authenticated(user)
                }.onFailure { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Falha na autenticação.")
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.value = AuthUiState.Error(t.message ?: "Erro ao processar login.")
            }
        }
    }

    fun register(name: String, email: String, pass: String, userType: UserType) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = userRepository.registerUser(name, email, pass, userType)
                result.onSuccess { user ->
                    encryptedStorageManager.saveAuthToken("jwt_token_${user.id}")
                    _uiState.value = AuthUiState.Authenticated(user)
                }.onFailure { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Erro ao cadastrar conta.")
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.value = AuthUiState.Error(t.message ?: "Erro ao cadastrar conta.")
            }
        }
    }

    fun linkCoach(athleteId: String, inviteCode: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = linkAthleteToCoachUseCase(athleteId, inviteCode)
                result.onSuccess { user ->
                    _uiState.value = AuthUiState.Authenticated(user)
                }.onFailure { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Erro ao vincular treinador")
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.value = AuthUiState.Error(t.message ?: "Erro ao vincular treinador.")
            }
        }
    }
}
