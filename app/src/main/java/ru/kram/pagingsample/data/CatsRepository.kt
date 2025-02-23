package ru.kram.pagingsample.data

import ru.kram.pagingsample.core.CatDispatchers
import ru.kram.pagingsample.data.db.local.CatLocalDao
import ru.kram.pagingsample.data.db.local.CatLocalEntity
import ru.kram.pagingsample.data.remote.CatsRemoteDataSource
import timber.log.Timber

class CatsRepository(
    private val dispatchers: CatDispatchers,
    private val catsRemoteDataSource: CatsRemoteDataSource,
    private val catLocalDao: CatLocalDao,
) {
    suspend fun clearLocal() = dispatchers.io {
        catLocalDao.clearAll()
    }

    suspend fun clearUserCats() = dispatchers.io {
        catLocalDao.clearAll()
        catsRemoteDataSource.clearUserCats()
    }

    suspend fun deleteCat(id: String) = dispatchers.io {
        catsRemoteDataSource.deleteCat(id)
        catLocalDao.deleteCat(id)
    }

    suspend fun addCat(createdAt: Long) = dispatchers.io {
        val catDTO = catsRemoteDataSource.addCat(createdAt)
        val mappedCat = catDTO?.let {
            CatLocalEntity(
                id = it.id,
                name = it.name,
                imageUrl = it.imageUrl,
                breed = it.breed,
                age = it.age,
                createdAt = it.createdAt,
            )
        }
        Timber.d("addCat: $mappedCat")
        mappedCat?.let { catLocalDao.insertAll(listOf(it)) }
    }

    suspend fun addCats(amount: Int) = dispatchers.io {
        val catsDTO = catsRemoteDataSource.addCats(amount)
        val mappedCats = catsDTO.map {
            CatLocalEntity(
                id = it.id,
                name = it.name,
                imageUrl = it.imageUrl,
                breed = it.breed,
                age = it.age,
                createdAt = it.createdAt,
            )
        }
        Timber.d("addCats: amountWanted=$amount, amountAdded=${mappedCats.size}")
        catLocalDao.insertAll(mappedCats)
    }
}