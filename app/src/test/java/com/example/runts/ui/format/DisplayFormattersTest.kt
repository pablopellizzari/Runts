package com.example.runts.ui.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DisplayFormattersTest {
    @Test
    fun translatesAndStructuresLegacyWorkoutSegments() {
        val content = """
            Intervalado progressivo

            Controle a respiração.

            1. WARMUP — 10 min — Z2
            2. INTERVAL — 5 min — Z4
            3. COOLDOWN — 8 min — Z1
        """.trimIndent()

        val result = workoutDescriptionPresentation(content, "Treino")

        assertEquals("Intervalado progressivo", result.title)
        assertEquals("Controle a respiração.", result.notes)
        assertEquals(listOf("Aquecimento", "Tiro", "Desaquecimento"), result.segments.map { it.type })
        assertFalse(result.segments.any { it.type.contains("WARMUP") || it.type.contains("INTERVAL") })
        assertEquals("10 min · Z2", result.segments.first().details)
    }
}
