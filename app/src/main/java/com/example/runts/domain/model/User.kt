package com.example.runts.domain.model

/**
 * Modelo de domínio para Usuário (Treinador ou Atleta).
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val userType: UserType,
    val coachId: String? = null,    // Vinculado se for Atleta
    val inviteCode: String? = null  // Código de convite se for Treinador
)
