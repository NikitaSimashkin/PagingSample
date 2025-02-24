package ru.kram.pagingsample.data.remote

import ru.kram.pagingsample.core.ServerDatabaseTransactionHelper
import ru.kram.pagingsample.data.db.server.persistent.CatPersistentDao
import ru.kram.pagingsample.data.db.server.user.UserCatDao
import ru.kram.pagingsample.data.db.server.user.UserCatEntity
import ru.kram.pagingsample.data.remote.model.CatDTO

class CatsRemoteDataSource(
    private val roomTransactionHelper: ServerDatabaseTransactionHelper,
    private val catPersistentDao: CatPersistentDao,
    private val userCatDao: UserCatDao,
) {
    suspend fun clearUserCats() = roomTransactionHelper.withTransaction {
        userCatDao.deleteAll()
    }

    suspend fun getCats(limit: Int, offset: Int): List<CatDTO> {
        val userCats = userCatDao.getUserCats(limit = limit, offset = offset)
        val userCatsIds = userCats.mapTo(mutableSetOf()) { it.catId }
        val cats = catPersistentDao.getByIds(userCatsIds).associateBy { it.id }

        return userCats.map { userCat ->
            val cat = cats[userCat.catId] ?: return@map null

            CatDTO(
                id = cat.id,
                imageUrl = cat.imageUrl,
                name = cat.name,
                breed = cat.breed,
                createdAt = userCat.createdAt,
                age = cat.age,
            )
        }.filterNotNull()
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
            )
        }.filterNotNull()
    }

    suspend fun addCat(createdAt: Long): CatDTO? {
        return addCats(1) { createdAt }.firstOrNull()
    }

    suspend fun addCats(amount: Int): List<CatDTO> {
        return addCats(amount) { System.currentTimeMillis() }
    }

    suspend fun deleteCat(id: String) = roomTransactionHelper.withTransaction {
        userCatDao.deleteCat(id)
    }

    private suspend fun addCats(
        amount: Int,
        random: Boolean = true,
        createdAt: () -> Long,
    ): List<CatDTO> = roomTransactionHelper.withTransaction {
        val allCatIds = catPersistentDao.getAllIds().toSet()
        val existingUserCatIds = userCatDao.getAllIds().toSet()

        val availableCats = allCatIds - existingUserCatIds
        if (availableCats.isEmpty()) return@withTransaction emptyList()

        val selectedCatsIds = (if (random) availableCats.shuffled() else availableCats.sorted()).take(amount)
        val selectedCats = selectedCatsIds
            .mapIndexed { index, id ->
                UserCatEntity(
                    catId = id,
                    createdAt = createdAt() + index
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
            )
        }
    }
}