package ru.kram.pagingsample.data.db.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface FilmLocalDao {

    @Upsert
    suspend fun insertAll(films: List<FilmLocalEntity>)

    @Query("DELETE FROM FilmLocalEntity")
    suspend fun clearAll()

    @Query("SELECT * FROM FilmLocalEntity ORDER BY createdAt DESC")
    suspend fun getLastFilm(): FilmLocalEntity?

    @Query("SELECT * FROM FilmLocalEntity ORDER BY createdAt ASC")
    suspend fun getFirstFilm(): FilmLocalEntity?

    @Query("SELECT * FROM FilmLocalEntity ORDER BY createdAt LIMIT :limit OFFSET :offset")
    suspend fun getFilms(limit: Int, offset: Int): List<FilmLocalEntity>

    @Query("Delete FROM FilmLocalEntity WHERE id = :id")
    suspend fun deleteFilm(id: String)
}