package ru.kram.pagingsample.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.CancellationException
import ru.kram.pagingsample.data.db.local.FilmLocalDao
import ru.kram.pagingsample.data.db.local.FilmLocalEntity
import ru.kram.pagingsample.data.remote.FilmsRemoteDataSource
import timber.log.Timber

@OptIn(ExperimentalPagingApi::class)
class FilmsRemoteMediator(
    private val basePageSize: Int,
    private val filmLocalDao: FilmLocalDao,
    private val filmsRemoteDataSource: FilmsRemoteDataSource,
) : RemoteMediator<Int, FilmLocalEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FilmLocalEntity>
    ): MediatorResult {
        return try {
            val loadSize = basePageSize
            val anchorPosition = state.anchorPosition
            val currentPage = anchorPosition?.let {
                state.closestPageToPosition(it)?.prevKey?.plus(1)
                    ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
                    ?: 0
            } ?: 0

            val page = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> currentPage - 1
                LoadType.APPEND -> currentPage + 1
            }

            val response = filmsRemoteDataSource.getFilms(
                offset = page * loadSize,
                limit = loadSize
            ).films

            filmLocalDao.insertAll(
                response.map {
                    FilmLocalEntity(
                        id = it.id,
                        imageUrl = it.imageUrl,
                        name = it.name,
                        age = it.year,
                        createdAt = it.createdAt,
                        number = it.number,
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
}