package ru.kram.pagingsample.data.db

import ru.kram.pagingsample.core.FilmDispatchers
import ru.kram.pagingsample.data.db.server.persistent.FilmPersistentDao

class ServerDatabaseInitializer(
    private val persistentTableLoader: PersistentTableLoader,
    private val filmPersistentDao: FilmPersistentDao,
    private val dispatchers: FilmDispatchers,
) {

    suspend fun init() = dispatchers.io {
        filmPersistentDao.clearAll()
        persistentTableLoader.loadFilms()
    }
}