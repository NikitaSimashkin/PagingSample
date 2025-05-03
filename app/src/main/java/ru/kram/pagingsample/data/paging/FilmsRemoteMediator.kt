package ru.kram.pagingsample.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.CancellationException
import ru.kram.pagingsample.data.db.local.FilmLocalDao
import ru.kram.pagingsample.data.db.local.FilmLocalEntity
import ru.kram.pagingsample.data.remote.FilmsRemoteDataSource
import ru.kram.pagingsample.domain.FilmDomain
import ru.kram.pagingsample.domain.FilmsRepository
import timber.log.Timber

@OptIn(ExperimentalPagingApi::class)
class FilmsRemoteMediator(
    private val basePageSize: Int,
    private val filmsRepository: FilmsRepository,
) : RemoteMediator<Int, FilmDomain>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FilmDomain>
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

            val response = filmsRepository.getFilms(
                offset = page * loadSize,
                limit = loadSize,
                fromNetwork = true,
            ).films

            Timber.d("load: response.size=${response.size}")

            MediatorResult.Success(endOfPaginationReached = response.size < loadSize)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}