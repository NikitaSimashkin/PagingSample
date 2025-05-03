package ru.kram.pagingsample.domain

import kotlinx.coroutines.flow.Flow

interface FilmsRepository {
    suspend fun clearLocal()
    suspend fun clearUserFilms()
    suspend fun deleteFilm(id: String)
    suspend fun addFilm()
    suspend fun addFilms(amount: Int)
    suspend fun getFilms(limit: Int, offset: Int, fromNetwork: Boolean): FilmsDomain
    fun observeFilmsCount(): Flow<Int>
    suspend fun updateTotalCount()
}