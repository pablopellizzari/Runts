package com.example.runts.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try { Result.success(block()) }
catch (e: CancellationException) { throw e }
catch (e: Exception) { Result.failure(e) }

/** Deliver Room immediately; the refresh never delays or terminates the local subscription. */
fun <T> offlineFlow(local: Flow<T>, refresh: suspend () -> Unit): Flow<T> = channelFlow {
    launch { resultOf { refresh() } }
    local.collect { send(it) }
}.distinctUntilChanged()
