package ru.kram.pagingsample.data.db.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import ru.kram.pagingsample.core.FilmDispatchers
import ru.kram.pagingsample.domain.FilmDomain
import ru.kram.pagingsample.domain.FilmsDomain

class FilmLocalDataSource(
    private val filmLocalDao: FilmLocalDao,
    private val dispatchers: FilmDispatchers,
    context: Context
) {
    private val totalCountPref = context.getSharedPreferences(
        TOTAL_COUNT_KEY,
        Context.MODE_PRIVATE
    )

    suspend fun getFilms(limit: Int, offset: Int): FilmsDomain = dispatchers.io {
        val films = filmLocalDao.getFilms(limit, offset)
        val filmsDomain = films.map { film ->
            FilmDomain(
                id = film.id,
                imageUrl = film.imageUrl,
                name = film.name,
                createdAt = film.createdAt,
                year = film.age,
                number = film.number,
            )
        }

        return@io FilmsDomain(
            films = filmsDomain,
            totalCount = getTotalCount(),
        )
    }

    suspend fun insertFilms(filmsDomain: FilmsDomain) = dispatchers.io {
        insertFilms(filmsDomain.films)
        totalCountPref.edit().putInt(TOTAL_COUNT_KEY, filmsDomain.totalCount).apply()
    }

    suspend fun setTotalCount(count: Int) = dispatchers.io {
        totalCountPref.edit().putInt(TOTAL_COUNT_KEY, count).apply()
    }

    suspend fun clearAll() = dispatchers.io {
        filmLocalDao.clearAll()
        totalCountPref.edit().putInt(TOTAL_COUNT_KEY, 0).apply()
    }

    suspend fun deleteFilm(id: String) = dispatchers.io {
        filmLocalDao.deleteFilm(id)
        val currentCount = getTotalCount()
        if (currentCount > 0) {
            totalCountPref.edit().putInt(TOTAL_COUNT_KEY, currentCount - 1).apply()
        }
    }

    suspend fun insertFilms(films: List<FilmDomain>) = dispatchers.io {
        val filmsLocal = films.map { film ->
            FilmLocalEntity(
                id = film.id,
                imageUrl = film.imageUrl,
                name = film.name,
                createdAt = film.createdAt,
                age = film.year,
                number = film.number,
            )
        }
        filmLocalDao.insertAll(filmsLocal)
    }

    private fun getTotalCount(): Int {
        return totalCountPref.getInt(TOTAL_COUNT_KEY, 0)
    }

    fun observeTotalCount(): Flow<Int> = callbackFlow {
        trySend(totalCountPref.getInt(TOTAL_COUNT_KEY, 0))
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == TOTAL_COUNT_KEY) {
                trySend(prefs.getInt(TOTAL_COUNT_KEY, 0))
            }
        }
        totalCountPref.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            totalCountPref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.flowOn(dispatchers.io)

    companion object {
        private const val TOTAL_COUNT_KEY = "total_count"
    }
}