package ru.kram.pagingsample.data.db.server

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.kram.pagingsample.data.db.server.persistent.FilmPersistentEntity
import ru.kram.pagingsample.data.db.server.persistent.FilmPersistentDao
import ru.kram.pagingsample.data.db.server.user.UserFilmDao
import ru.kram.pagingsample.data.db.server.user.UserFilmEntity

@Database(
    entities = [
        FilmPersistentEntity::class,
        UserFilmEntity::class,
    ],
    version = 3
)
abstract class FilmServerDatabase : RoomDatabase() {
    abstract fun filmPersistentDao(): FilmPersistentDao
    abstract fun userFilmDao(): UserFilmDao

    companion object {
        const val DATABASE_NAME = "film_server_database"
    }
}