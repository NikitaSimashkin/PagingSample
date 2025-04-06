package ru.kram.pagingsample.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.CancellationException
import ru.kram.pagingsample.data.db.local.CatLocalDao
import ru.kram.pagingsample.data.db.local.CatLocalEntity
import ru.kram.pagingsample.data.remote.CatsRemoteDataSource
import timber.log.Timber

@OptIn(ExperimentalPagingApi::class)
class CatsRemoteMediator : RemoteMediator<Int, CatLocalEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CatLocalEntity>
    ): MediatorResult {
        TODO()
    }
}