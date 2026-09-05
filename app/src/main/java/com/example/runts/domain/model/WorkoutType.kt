package com.example.runts.domain.model

/**
 * Modalidades/Tipos de treino de corrida.
 */
enum class WorkoutType {
    RODAGEM,        // Corrida contínua leve/moderada
    TIROS,          // Treino intervalado de velocidade
    TEMPO_RUN,      // Corrida em ritmo de limiar anaeróbico
    LONGAO,         // Treino de longa distância
    FORTALECIMENTO, // Exercícios funcionais/musculação
    PROVA           // Simulado ou competição
}
