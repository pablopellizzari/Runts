package com.example.runts

import com.example.runts.data.remote.database.NeonPostgresManager
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Assert.assertTrue
import org.junit.Test

/** Opt-in integration test; no database credential is stored in the repository. */
class NeonConnectionTest {
    @Test
    fun createsSchemaWhenNeonIsConfigured() = runBlocking {
        assumeTrue(BuildConfig.NEON_HOST.isNotBlank() && BuildConfig.NEON_PASSWORD.isNotBlank())
        val result = NeonPostgresManager().initDatabaseTables()
        assertTrue(result.exceptionOrNull()?.message.orEmpty(), result.isSuccess)
    }
}
