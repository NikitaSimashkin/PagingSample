package ru.kram.pagingsample.core

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import ru.kram.pagingsample.data.db.server.CatServerDatabase

abstract class RoomTransactionHelper(
    private val database: RoomDatabase,
    private val catDispatchers: CatDispatchers,
) {

    suspend fun <T> withTransaction(block: suspend () -> T): T {
        return catDispatchers.io {
            database.withTransaction {
                block()
            }
        }
    }
}

class ServerDatabaseTransactionHelper(
    database: CatServerDatabase,
    catDispatchers: CatDispatchers,
) : RoomTransactionHelper(database, catDispatchers)

class LocalDatabaseTransactionHelper(
    database: CatServerDatabase,
    catDispatchers: CatDispatchers,
) : RoomTransactionHelper(database, catDispatchers)