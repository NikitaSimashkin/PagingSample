package ru.kram.pagingsample.core

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import ru.kram.pagingsample.data.db.server.FilmServerDatabase

abstract class RoomTransactionHelper(
    private val database: RoomDatabase,
    private val filmDispatchers: FilmDispatchers,
) {

    suspend fun <T> withTransaction(block: suspend () -> T): T {
        return filmDispatchers.io {
            database.withTransaction {
                block()
            }
        }
    }
}

class ServerDatabaseTransactionHelper(
    database: FilmServerDatabase,
    filmDispatchers: FilmDispatchers,
) : RoomTransactionHelper(database, filmDispatchers)

class LocalDatabaseTransactionHelper(
    database: FilmServerDatabase,
    filmDispatchers: FilmDispatchers,
) : RoomTransactionHelper(database, filmDispatchers)