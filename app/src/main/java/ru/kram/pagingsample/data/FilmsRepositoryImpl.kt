package ru.kram.pagingsample.data

import kotlinx.coroutines.flow.Flow
import ru.kram.pagingsample.core.FilmDispatchers
import ru.kram.pagingsample.data.db.local.FilmLocalDataSource
import ru.kram.pagingsample.data.remote.FilmsRemoteDataSource
import ru.kram.pagingsample.domain.FilmsDomain
import ru.kram.pagingsample.domain.FilmsRepository

class FilmsRepositoryImpl(
    private val dispatchers: FilmDispatchers,
    private val filmsRemoteDataSource: FilmsRemoteDataSource,
    private val filmLocalDataSource: FilmLocalDataSource,
): FilmsRepository {

    override suspend fun clearLocal() = dispatchers.io {
        filmLocalDataSource.clearAll()
    }

    override suspend fun clearUserFilms() = dispatchers.io {
        filmLocalDataSource.clearAll()
        filmsRemoteDataSource.clearUserFilms()
    }

    override suspend fun deleteFilm(id: String) = dispatchers.io {
        filmsRemoteDataSource.deleteFilm(id)
        filmLocalDataSource.deleteFilm(id)
    }

    override suspend fun addFilm() = dispatchers.io {
        filmsRemoteDataSource.addFilm()
        updateTotalCount()
    }

    override suspend fun addFilms(amount: Int) = dispatchers.io {
        filmsRemoteDataSource.addFilms(amount)
        updateTotalCount()
    }

    override suspend fun getFilms(limit: Int, offset: Int, fromNetwork: Boolean): FilmsDomain {
        return if (fromNetwork) {
            val films = filmsRemoteDataSource.getFilms(limit = limit, offset = offset)
            filmLocalDataSource.insertFilms(films)
            films
        } else {
            val films = filmLocalDataSource.getFilms(limit = limit, offset = offset)
            films
        }
    }

    override fun observeFilmsCount(): Flow<Int> {
        return filmLocalDataSource.observeTotalCount()
    }

    override suspend fun updateTotalCount() {
        val totalCount = filmsRemoteDataSource.getFilms(limit = 1, offset = 0).totalCount
        filmLocalDataSource.setTotalCount(totalCount)
    }
}