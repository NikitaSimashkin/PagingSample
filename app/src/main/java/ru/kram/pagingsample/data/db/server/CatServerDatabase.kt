package ru.kram.pagingsample.data.db.server

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.kram.pagingsample.data.db.server.persistent.CatPersistentEntity
import ru.kram.pagingsample.data.db.server.persistent.CatPersistentDao
import ru.kram.pagingsample.data.db.server.user.UserCatDao
import ru.kram.pagingsample.data.db.server.user.UserCatEntity

@Database(
    entities = [
        CatPersistentEntity::class,
        UserCatEntity::class,
    ],
    version = 1
)
abstract class CatServerDatabase : RoomDatabase() {
    abstract fun catPersistentDao(): CatPersistentDao
    abstract fun userCatDao(): UserCatDao

    companion object {
        const val DATABASE_NAME = "cat_server_database"
    }
}