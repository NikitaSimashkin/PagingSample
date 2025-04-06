package ru.kram.pagingsample.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import ru.kram.pagingsample.core.ServerDatabaseTransactionHelper
import ru.kram.pagingsample.data.db.server.persistent.CatPersistentDao
import ru.kram.pagingsample.data.db.server.user.UserCatDao
import ru.kram.pagingsample.data.db.server.user.UserCatEntity
import ru.kram.pagingsample.data.remote.model.CatDTO
import ru.kram.pagingsample.domain.CatsDomain

class CatsRemoteDataSource(
    private val roomTransactionHelper: ServerDatabaseTransactionHelper,
    private val catPersistentDao: CatPersistentDao,
    private val userCatDao: UserCatDao,
) {
    suspend fun clearUserCats() = roomTransactionHelper.withTransaction {
        userCatDao.deleteAll()
    }

    suspend fun getCats(limit: Int, offset: Int): CatsDomain {
        delay(500)
        return roomTransactionHelper.withTransaction {
            val userCats = userCatDao.getUserCats(limit = limit, offset = offset)
            val userCatsIds = userCats.mapTo(mutableSetOf()) { it.catId }
            val cats = catPersistentDao.getByIds(userCatsIds).associateBy { it.id }

            val mapepdCats = userCats.map { userCat ->
                val cat = cats[userCat.catId] ?: return@map null

                CatDTO(
                    id = cat.id,
                    imageUrl = cat.imageUrl,
                    name = cat.name,
                    breed = cat.breed,
                    createdAt = userCat.createdAt,
                    age = cat.age,
                    number = userCat.number,
                )
            }.filterNotNull()

            val itemsBefore = mapepdCats.firstOrNull()?.let { userCatDao.getUserCatsCountBefore(it.createdAt) }
            val itemsAfter = mapepdCats.lastOrNull()?.let { userCatDao.getUserCatsCountAfter(it.createdAt) }

            CatsDomain(
                cats = mapepdCats,
                itemsBefore = itemsBefore,
                itemsAfter = itemsAfter,
            )
        }
    }

    fun getCatsCount(): Flow<Int> {
        return userCatDao.getUserCatsCount()
    }

    suspend fun getCats(
        createdAt: Long,
        limit: Int,
        lastCatId: String,
        isForward: Boolean,
    ): List<CatDTO> = roomTransactionHelper.withTransaction {
        val userCats = if (isForward) {
            userCatDao.getUserCatsAfter(createdAt, lastCatId, limit)
        } else {
            userCatDao.getUserCatsBefore(createdAt, lastCatId, limit)
        }

        val userCatsIds = userCats.mapTo(mutableSetOf()) { it.catId }

        val cats = catPersistentDao.getByIds(userCatsIds).associateBy { it.id }

        return@withTransaction userCats.map { userCat ->
            val cat = cats[userCat.catId] ?: return@map null

            CatDTO(
                id = cat.id,
                imageUrl = cat.imageUrl,
                name = cat.name,
                breed = cat.breed,
                createdAt = userCat.createdAt,
                age = cat.age,
                number = userCat.number,
            )
        }.filterNotNull()
    }

    suspend fun addCat(): CatDTO? {
        return addCats(1).firstOrNull()
    }

    suspend fun addCats(amount: Int): List<CatDTO> {
        return addCatsInternal(amount)
    }

    suspend fun deleteCat(id: String) = roomTransactionHelper.withTransaction {
        userCatDao.deleteCat(id)
    }

    private suspend fun addCatsInternal(
        amount: Int,
    ): List<CatDTO> = roomTransactionHelper.withTransaction {
        val allCatIds = catPersistentDao.getAllIds().toSet()
        val existingUserCatIds = userCatDao.getAllIds().toSet()

        val availableCats = allCatIds - existingUserCatIds
        if (availableCats.isEmpty()) return@withTransaction emptyList()

        val selectedCatsIds = availableCats.shuffled().take(amount)
        val selectedCats = selectedCatsIds
            .mapIndexed { index, id ->
                UserCatEntity(
                    catId = id,
                    createdAt = System.currentTimeMillis() + index,
                    number = existingUserCatIds.size + index,
                )
            }.associateBy { it.catId }

        userCatDao.insertAll(selectedCats.values)

        val fullCats = catPersistentDao.getByIds(selectedCatsIds)

        return@withTransaction fullCats.map {
            CatDTO(
                id = it.id,
                imageUrl = it.imageUrl,
                name = it.name,
                breed = it.breed,
                createdAt = selectedCats[it.id]?.createdAt ?: -1,
                age = it.age,
                number = selectedCats[it.id]?.number ?: -1,
            )
        }
    }
}