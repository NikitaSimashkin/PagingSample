package ru.kram.pagingsample.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.CancellationException
import ru.kram.pagingsample.data.db.local.CatLocalDao
import ru.kram.pagingsample.data.db.local.CatLocalEntity
import ru.kram.pagingsample.data.paging.model.CatPagingKey
import ru.kram.pagingsample.data.remote.CatsRemoteDataSource
import timber.log.Timber

@OptIn(ExperimentalPagingApi::class)
class CatsRemoteMediator(
    private val catLocalDao: CatLocalDao,
    private val catsRemoteDataSource: CatsRemoteDataSource,
) : RemoteMediator<CatPagingKey, CatLocalEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<CatPagingKey, CatLocalEntity>
    ): MediatorResult {
        return try {
            val loadSize = state.config.pageSize

            val lastItem = state.lastItemOrNull() ?: catLocalDao.getLastCat()
            val firstItem = state.firstItemOrNull() ?: catLocalDao.getFirstCat()

            val (createdAt, isForward) = when (loadType) {
                LoadType.REFRESH -> 0L to true
                LoadType.APPEND -> {
                    if (lastItem == null) return MediatorResult.Success(endOfPaginationReached = true)
                    lastItem.createdAt to true
                }
                LoadType.PREPEND -> {
                    if (firstItem == null) return MediatorResult.Success(endOfPaginationReached = true)
                    firstItem.createdAt to false
                }
            }

            Timber.d("load: createdAt=$createdAt, isForward=$isForward, loadSize=$loadSize")

            val response = catsRemoteDataSource.getCats(
                createdAt = createdAt,
                isForward = isForward,
                limit = loadSize,
                lastCatId = if (loadType == LoadType.PREPEND) state.firstItemOrNull()?.id ?: "" else state.lastItemOrNull()?.id ?: ""
            )

            catLocalDao.insertAll(
                response.map {
                    CatLocalEntity(
                        id = it.id,
                        imageUrl = it.imageUrl,
                        name = it.name,
                        breed = it.breed,
                        age = it.age,
                        createdAt = it.createdAt
                    )
                }
            )

            Timber.d("load: response.size=${response.size}")

            MediatorResult.Success(endOfPaginationReached = response.size < loadSize)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }
}