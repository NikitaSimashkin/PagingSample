package ru.kram.pagingsample.data.db.server.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserFilmDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFilm(userFilm: UserFilmEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(userFilms: Collection<UserFilmEntity>)

    @Delete
    suspend fun removeFilm(userFilm: UserFilmEntity)

    @Query("DELETE FROM UserFilmEntity")
    suspend fun deleteAll()

    @Query("DELETE FROM UserFilmEntity WHERE filmId = :filmId")
    suspend fun deleteFilm(filmId: String)

    @Query("SELECT filmId FROM UserFilmEntity")
    suspend fun getAllIds(): List<String>

    @Query("""
        SELECT * FROM UserFilmEntity 
        WHERE (createdAt > :createdAfter) OR (createdAt = :createdAfter AND filmId > :lastId)
        ORDER BY createdAt, filmId
        LIMIT :limit
    """)
    suspend fun getUserFilmsAfter(createdAfter: Long, lastId: String, limit: Int): List<UserFilmEntity>

    @Query("""
        SELECT * FROM UserFilmEntity 
        WHERE (createdAt < :createdBefore) OR (createdAt = :createdBefore AND filmId < :lastId)
        ORDER BY createdAt DESC, filmId DESC
        LIMIT :limit
    """)
    suspend fun getUserFilmsBefore(createdBefore: Long, lastId: String, limit: Int): List<UserFilmEntity>

    @Query("SELECT * FROM UserFilmEntity ORDER BY createdAt LIMIT :limit OFFSET :offset")
    suspend fun getUserFilms(limit: Int, offset: Int): List<UserFilmEntity>

    @Query("SELECT COUNT(*) FROM UserFilmEntity WHERE createdAt < :createdAt")
    suspend fun getUserFilmsCountBefore(createdAt: Long): Int

    @Query("SELECT COUNT(*) FROM UserFilmEntity WHERE createdAt > :createdAt")
    suspend fun getUserFilmsCountAfter(createdAt: Long): Int

    @Query("SELECT COUNT(*) FROM UserFilmEntity")
    fun observeUserFilmsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM UserFilmEntity")
    suspend fun getUserFilmsCount(): Int
}