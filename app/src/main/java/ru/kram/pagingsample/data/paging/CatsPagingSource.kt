package ru.kram.pagingsample.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import ru.kram.pagingsample.data.db.local.CatLocalDao
import ru.kram.pagingsample.data.db.local.CatLocalEntity
import ru.kram.pagingsample.data.paging.model.CatPagingKey
import timber.log.Timber

class CatsPagingSource(
    private val catLocalDao: CatLocalDao
) : PagingSource<CatPagingKey, CatLocalEntity>() {

    override suspend fun load(params: LoadParams<CatPagingKey>): LoadResult<CatPagingKey, CatLocalEntity> {
        return try {
            val loadSize = params.loadSize
            val key = params.key

            Timber.d("load: key=$key, loadSize=$loadSize, params=${params::class.simpleName}")

            val (cats, isForward) = when (params) {
                is LoadParams.Refresh -> {
                    val cats = (key ?: CatPagingKey(0, "", "")).let {
                        catLocalDao.getCatsForward(it.createdAt, it.catId, loadSize)
                    }
                    cats to true
                }
                is LoadParams.Append -> {
                    val cats = key?.let {
                        catLocalDao.getCatsForward(it.createdAt, it.catId, loadSize)
                    } ?: emptyList()
                    cats to true
                }
                is LoadParams.Prepend -> {
                    val cats = key?.let {
                        catLocalDao.getCatsBackward(it.createdAt, it.catId, loadSize).reversed()
                    } ?: emptyList()
                    cats to false
                }
            }

            Timber.d("load: cats=${cats.joinToString(",") { it.name }}")

            val prevKey = if (params is LoadParams.Prepend && cats.isEmpty()) {
                null
            } else {
                cats.firstOrNull()?.let {
                    CatPagingKey(it.createdAt, it.id, it.name)
                }
            }

            val nextKey = if (params is LoadParams.Append && cats.isEmpty()) {
                null
            } else {
                cats.lastOrNull()?.let {
                    CatPagingKey(it.createdAt, it.id, it.name)
                }
            }

            Timber.d("load, prevKey=$prevKey, nextKey=$nextKey")
            LoadResult.Page(
                data = cats,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<CatPagingKey, CatLocalEntity>): CatPagingKey? {
        val anchor = ((state.anchorPosition ?: 0) - state.config.pageSize / 2).coerceAtLeast(0)
        val key = state.closestItemToPosition(anchor)?.let {
            CatPagingKey(it.createdAt - 1, it.id, it.name)
        }

        Timber.d("getRefreshKey: name=${key?.catName}, key=$key")
        return key
    }

    class Factory(
        private val catLocalDao: CatLocalDao
    ) : () -> CatsPagingSource {
        override fun invoke(): CatsPagingSource {
            return CatsPagingSource(catLocalDao)
        }
    }
}