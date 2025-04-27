package ru.kram.pagingsample.data.db

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CancellationException
import ru.kram.pagingsample.core.FilmDispatchers
import ru.kram.pagingsample.data.db.server.persistent.FilmPersistentDao
import ru.kram.pagingsample.data.db.server.persistent.FilmPersistentEntity
import timber.log.Timber
import java.util.UUID

class PersistentTableLoader(
    private val context: Context,
    private val gson: Gson,
    private val filmPersistentDao: FilmPersistentDao,
    private val dispatchers: FilmDispatchers,
) {

    suspend fun loadFilms() = dispatchers.io {
        val file = "films.json"

        try {
            val json = context.assets.open(file).bufferedReader().use { it.readText() }
            val films: List<FilmJson> = gson.fromJson(
                json,
                (object : TypeToken<List<FilmJson>>() {}).type
            )

            films.map { film ->
                FilmPersistentEntity(
                    id = UUID.randomUUID().toString(),
                    name = film.title,
                    age = film.year.toInt(),
                    imageUrl = film.posterUrl,
                )
            }.apply {
                filmPersistentDao.insertAll(this)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    private data class FilmJson(val posterUrl: String, val title: String, val year: String, val director: String?)
}