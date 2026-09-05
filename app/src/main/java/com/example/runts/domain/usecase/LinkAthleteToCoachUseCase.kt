package com.example.runts.domain.usecase

import com.example.runts.domain.model.User
import com.example.runts.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de Uso (RF01): Lógica para vincular o perfil do atleta a um treinador por meio do código de convite.
 */
class LinkAthleteToCoachUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(athleteId: String, inviteCode: String): Result<User> {
        if (inviteCode.isBlank()) {
            return Result.failure(IllegalArgumentException("O código de convite não pode estar vazio."))
        }
        return userRepository.linkAthleteToCoach(athleteId, inviteCode.trim())
    }
}
