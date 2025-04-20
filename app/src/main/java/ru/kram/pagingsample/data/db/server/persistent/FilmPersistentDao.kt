package ru.kram.pagingsample.data.db.server.persistent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FilmPersistentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(films: List<FilmPersistentEntity>)

    @Query("DELETE FROM FilmPersistentEntity")
    suspend fun clearAll()

    @Query("SELECT id FROM FilmPersistentEntity")
    suspend fun getAllIds(): List<String>

    @Query("SELECT * FROM FilmPersistentEntity WHERE id in (:ids)")
    suspend fun getByIds(ids: Collection<String>): List<FilmPersistentEntity>
}