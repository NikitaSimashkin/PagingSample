package ru.kram.pagingsample.data.db.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface CatLocalDao {

    @Upsert
    suspend fun insertAll(cats: List<CatLocalEntity>)

    @Query(
        """
        SELECT * FROM CatLocalEntity 
        WHERE (createdAt > :createdAfter) OR (createdAt = :createdAfter AND id > :lastId)
        ORDER BY createdAt, id
        LIMIT :limit
    """
    )
    suspend fun getCatsForward(createdAfter: Long, lastId: String, limit: Int): List<CatLocalEntity>

    @Query("""
        SELECT * FROM CatLocalEntity 
        WHERE (createdAt < :createdBefore) OR (createdAt = :createdBefore AND id < :lastId)
        ORDER BY createdAt DESC, id DESC
        LIMIT :limit
    """)
    suspend fun getCatsBackward(createdBefore: Long, lastId: String, limit: Int): List<CatLocalEntity>

    @Query("DELETE FROM CatLocalEntity")
    suspend fun clearAll()

    @Query("SELECT * FROM CatLocalEntity ORDER BY createdAt DESC")
    suspend fun getLastCat(): CatLocalEntity?

    @Query("SELECT * FROM CatLocalEntity ORDER BY createdAt ASC")
    suspend fun getFirstCat(): CatLocalEntity?

    @Query("SELECT * FROM CatLocalEntity ORDER BY createdAt LIMIT :limit OFFSET :offset")
    suspend fun getCats(limit: Int, offset: Int): List<CatLocalEntity>

    @Query("Delete FROM CatLocalEntity WHERE id = :id")
    suspend fun deleteCat(id: String)
}