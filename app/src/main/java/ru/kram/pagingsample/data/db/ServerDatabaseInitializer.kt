package ru.kram.pagingsample.data.db

import ru.kram.pagingsample.core.CatDispatchers
import ru.kram.pagingsample.data.db.server.persistent.CatPersistentDao

class ServerDatabaseInitializer(
    private val persistentTableLoader: PersistentTableLoader,
    private val catPersistentDao: CatPersistentDao,
    private val dispatchers: CatDispatchers,
) {

    suspend fun init() = dispatchers.io {
        catPersistentDao.clearAll()
        persistentTableLoader.loadCats()
    }
}