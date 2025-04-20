package ru.kram.pagingsample.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

interface FilmDispatchers {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher

    fun createIoScope() = CoroutineScope(io + SupervisorJob())
    fun createMainScope() = CoroutineScope(main + SupervisorJob())

    suspend fun <T> io(block: suspend CoroutineScope.() -> T) = withContext(io, block)
    suspend fun <T> main(block: suspend CoroutineScope.() -> T) = withContext(main, block)
}

class DefaultFilmDispatchers: FilmDispatchers {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val main: CoroutineDispatcher = Dispatchers.Main
}