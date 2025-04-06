package ru.kram.pagingsample.data.db.server.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.kram.pagingsample.data.db.local.CatLocalEntity

@Dao
interface UserCatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCat(userCat: UserCatEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(userCats: Collection<UserCatEntity>)

    @Delete
    suspend fun removeCat(userCat: UserCatEntity)

    @Query("DELETE FROM UserCatEntity")
    suspend fun deleteAll()

    @Query("DELETE FROM UserCatEntity WHERE catId = :catId")
    suspend fun deleteCat(catId: String)

    @Query("SELECT catId FROM UserCatEntity")
    suspend fun getAllIds(): List<String>

    @Query("""
        SELECT * FROM UserCatEntity 
        WHERE (createdAt > :createdAfter) OR (createdAt = :createdAfter AND catId > :lastId)
        ORDER BY createdAt, catId
        LIMIT :limit
    """)
    suspend fun getUserCatsAfter(createdAfter: Long, lastId: String, limit: Int): List<UserCatEntity>

    @Query("""
        SELECT * FROM UserCatEntity 
        WHERE (createdAt < :createdBefore) OR (createdAt = :createdBefore AND catId < :lastId)
        ORDER BY createdAt DESC, catId DESC
        LIMIT :limit
    """)
    suspend fun getUserCatsBefore(createdBefore: Long, lastId: String, limit: Int): List<UserCatEntity>

    @Query("SELECT * FROM UserCatEntity ORDER BY createdAt LIMIT :limit OFFSET :offset")
    suspend fun getUserCats(limit: Int, offset: Int): List<UserCatEntity>

    @Query("SELECT COUNT(*) FROM UserCatEntity WHERE createdAt < :createdAt")
    suspend fun getUserCatsCountBefore(createdAt: Long): Int

    @Query("SELECT COUNT(*) FROM UserCatEntity WHERE createdAt > :createdAt")
    suspend fun getUserCatsCountAfter(createdAt: Long): Int

    @Query("SELECT COUNT(*) FROM UserCatEntity")

    fun getUserCatsCount(): Flow<Int>
}