package ru.kram.pagingsample.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import ru.kram.pagingsample.data.db.local.CatLocalDao
import ru.kram.pagingsample.data.db.local.CatLocalEntity
import timber.log.Timber

class CatsPagingSource(
    private val basePageSize: Int,
    private val catLocalDao: CatLocalDao
) : PagingSource<Int, CatLocalEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CatLocalEntity> {
        return try {
            val loadSize = basePageSize
            val key = params.key ?: 0

            Timber.d("load: key=$key, loadSize=$loadSize, params=${params::class.simpleName}")

            val cats = catLocalDao.getCats(limit = loadSize, offset = key * loadSize)

            return LoadResult.Page(
                data = cats,
                prevKey = if (key == 0) null else key - 1,
                nextKey = if (cats.isEmpty()) null else key + 1
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CatLocalEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?:
            state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1) ?: 0
        }
    }

    class Factory(
        private val basePageSize: Int,
        private val catLocalDao: CatLocalDao
    ) : () -> CatsPagingSource {
        override fun invoke(): CatsPagingSource {
            return CatsPagingSource(basePageSize, catLocalDao)
        }
    }
}