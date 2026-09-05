package com.example.runts

import com.example.runts.domain.model.TrainingRules
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingRulesTest {
    @Test fun parsesDurationsAndCalculatesPace() {
        assertEquals(2679, TrainingRules.duration("00:44:39"))
        assertEquals("5:30", TrainingRules.pace(8.12, 2679))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidHeartRateZone() {
        TrainingRules.targets(8.0, 45, "5:30", "Z6")
    }
}
