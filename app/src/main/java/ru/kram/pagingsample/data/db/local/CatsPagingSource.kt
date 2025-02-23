package ru.kram.pagingsample.data.db.local

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
            when(params) {
                is LoadParams.Refresh -> TODO()
                is LoadParams.Append -> TODO()
                is LoadParams.Prepend -> {
                    LoadResult.Page(
                        prevKey = null,
                        nextKey = null,
                        data = emptyList()
                    )
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<CatPagingKey, CatLocalEntity>): CatPagingKey? {
        TODO()
    }
}