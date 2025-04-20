package ru.kram.pagingsample.data.db.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FilmLocalEntity::class,
    ],
    version = 3
)
abstract class FilmLocalDatabase : RoomDatabase() {
    abstract fun filmLocalDao(): FilmLocalDao

    companion object {
        const val DATABASE_NAME = "film_local_database"
    }
}