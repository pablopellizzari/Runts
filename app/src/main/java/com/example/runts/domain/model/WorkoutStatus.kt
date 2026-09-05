package com.example.runts.domain.model

/**
 * Status de prescrição do treino.
 */
enum class WorkoutStatus {
    PENDING,   // Pendente
    COMPLETED, // Concluído pelo atleta
    SKIPPED    // Não realizado
}
