package com.example.runts.domain.model

data class StravaConnectionStatus(
    val connected: Boolean,
    val athleteName: String? = null,
    val lastSyncAt: String? = null
)

data class StravaSyncResult(
    val imported: Int,
    val matched: Int
)
