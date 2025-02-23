package ru.kram.pagingsample.data.db.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CatLocalEntity::class,
    ],
    version = 1
)
abstract class CatLocalDatabase : RoomDatabase() {
    abstract fun catLocalDao(): CatLocalDao

    companion object {
        const val DATABASE_NAME = "cat_local_database"
    }
}