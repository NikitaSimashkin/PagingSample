package ru.kram.pagingsample.data.remote

import kotlinx.coroutines.delay
import ru.kram.pagingsample.core.ServerDatabaseTransactionHelper
import ru.kram.pagingsample.data.db.server.persistent.FilmPersistentDao
import ru.kram.pagingsample.data.db.server.user.UserFilmDao
import ru.kram.pagingsample.data.db.server.user.UserFilmEntity
import ru.kram.pagingsample.data.remote.model.FilmDTO
import ru.kram.pagingsample.domain.FilmDomain
import ru.kram.pagingsample.domain.FilmsDomain

class FilmsRemoteDataSource(
    private val roomTransactionHelper: ServerDatabaseTransactionHelper,
    private val filmPersistentDao: FilmPersistentDao,
    private val userFilmDao: UserFilmDao,
) {
    suspend fun clearUserFilms() = roomTransactionHelper.withTransaction {
        userFilmDao.deleteAll()
    }

    suspend fun getFilms(limit: Int, offset: Int): FilmsDomain {
        delay(500)
        return roomTransactionHelper.withTransaction {
            val userFilms = userFilmDao.getUserFilms(limit = limit, offset = offset)
            val userFilmsIds = userFilms.mapTo(mutableSetOf()) { it.filmId }
            val films = filmPersistentDao.getByIds(userFilmsIds).associateBy { it.id }

            val mapepdFilms = userFilms.map { userFilm ->
                val film = films[userFilm.filmId] ?: return@map null

                FilmDomain(
                    id = film.id,
                    imageUrl = film.imageUrl,
                    name = film.name,
                    createdAt = userFilm.createdAt,
                    year = film.age,
                    number = userFilm.number,
                )
            }.filterNotNull()

            val totalCount = userFilmDao.getUserFilmsCount()

            FilmsDomain(
                films = mapepdFilms,
                totalCount = totalCount,
            )
        }
    }

    suspend fun addFilm(): FilmDTO? {
        return addFilms(1).firstOrNull()
    }

    suspend fun addFilms(amount: Int): List<FilmDTO> {
        return addFilmsInternal(amount)
    }

    suspend fun deleteFilm(id: String) = roomTransactionHelper.withTransaction {
        userFilmDao.deleteFilm(id)
    }

    private suspend fun addFilmsInternal(
        amount: Int,
    ): List<FilmDTO> = roomTransactionHelper.withTransaction {
        val allFilmIds = filmPersistentDao.getAllIds().toSet()
        val existingUserFilmIds = userFilmDao.getAllIds().toSet()

        val availableFilms = allFilmIds - existingUserFilmIds
        if (availableFilms.isEmpty()) return@withTransaction emptyList()

        val selectedFilmsIds = availableFilms.shuffled().take(amount)
        val selectedFilms = selectedFilmsIds
            .mapIndexed { index, id ->
                UserFilmEntity(
                    filmId = id,
                    createdAt = System.currentTimeMillis() + index,
                    number = existingUserFilmIds.size + index,
                )
            }.associateBy { it.filmId }

        userFilmDao.insertAll(selectedFilms.values)

        val fullFilms = filmPersistentDao.getByIds(selectedFilmsIds)

        return@withTransaction fullFilms.map {
            FilmDTO(
                id = it.id,
                imageUrl = it.imageUrl,
                name = it.name,
                createdAt = selectedFilms[it.id]?.createdAt ?: -1,
                age = it.age,
                number = selectedFilms[it.id]?.number ?: -1,
            )
        }
    }
}