package ru.kram.pagingsample.data.db.server.persistent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CatPersistentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cats: List<CatPersistentEntity>)

    @Query("DELETE FROM CatPersistentEntity")
    suspend fun clearAll()

    @Query("SELECT id FROM CatPersistentEntity")
    suspend fun getAllIds(): List<String>

    @Query("SELECT * FROM CatPersistentEntity WHERE id in (:ids)")
    suspend fun getByIds(ids: Collection<String>): List<CatPersistentEntity>
}