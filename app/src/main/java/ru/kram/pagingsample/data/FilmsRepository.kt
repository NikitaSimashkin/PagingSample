package ru.kram.pagingsample.data

import kotlinx.coroutines.flow.Flow
import ru.kram.pagingsample.core.FilmDispatchers
import ru.kram.pagingsample.data.db.local.FilmLocalDao
import ru.kram.pagingsample.data.db.local.FilmLocalDataSource
import ru.kram.pagingsample.data.db.local.FilmLocalEntity
import ru.kram.pagingsample.data.remote.FilmsRemoteDataSource
import ru.kram.pagingsample.domain.FilmsDomain
import timber.log.Timber

class FilmsRepository(
    private val dispatchers: FilmDispatchers,
    private val filmsRemoteDataSource: FilmsRemoteDataSource,
    private val filmLocalDataSource: FilmLocalDataSource,
) {
    suspend fun clearLocal() = dispatchers.io {
        filmLocalDataSource.clearAll()
    }

    suspend fun clearUserFilms() = dispatchers.io {
        filmLocalDataSource.clearAll()
        filmsRemoteDataSource.clearUserFilms()
    }

    suspend fun deleteFilm(id: String) = dispatchers.io {
        filmsRemoteDataSource.deleteFilm(id)
        filmLocalDataSource.deleteFilm(id)
    }

    suspend fun addFilm() = dispatchers.io {
        filmsRemoteDataSource.addFilm()
        updateTotalCount()
    }

    suspend fun addFilms(amount: Int) = dispatchers.io {
        filmsRemoteDataSource.addFilms(amount)
        updateTotalCount()
    }

    suspend fun getFilms(limit: Int, offset: Int, fromNetwork: Boolean): FilmsDomain {
        return if (fromNetwork) {
            val films = filmsRemoteDataSource.getFilms(limit = limit, offset = offset)
            filmLocalDataSource.insertFilms(films)
            films
        } else {
            val films = filmLocalDataSource.getFilms(limit = limit, offset = offset)
            films
        }
    }

    fun observeFilmsCount(): Flow<Int> {
        return filmLocalDataSource.observeTotalCount()
    }

    suspend fun updateTotalCount() {
        val totalCount = filmsRemoteDataSource.getFilms(limit = 1, offset = 0).totalCount
        filmLocalDataSource.setTotalCount(totalCount)
    }
}