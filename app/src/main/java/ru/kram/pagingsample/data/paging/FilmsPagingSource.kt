package ru.kram.pagingsample.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import ru.kram.pagingsample.data.db.local.FilmLocalDao
import ru.kram.pagingsample.data.db.local.FilmLocalEntity
import ru.kram.pagingsample.domain.FilmDomain
import ru.kram.pagingsample.domain.FilmsRepository
import timber.log.Timber

class FilmsPagingSource(
    private val basePageSize: Int,
    private val filmsRepository: FilmsRepository
) : PagingSource<Int, FilmDomain>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FilmDomain> {
        return try {
            val loadSize = basePageSize
            val key = params.key ?: 0

            Timber.d("load: key=$key, loadSize=$loadSize, params=${params::class.simpleName}")

            val films = filmsRepository.getFilms(limit = loadSize, offset = key * loadSize, fromNetwork = true).films

            return LoadResult.Page(
                data = films,
                prevKey = if (key == 0) null else key - 1,
                nextKey = if (films.isEmpty()) null else key + 1
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FilmDomain>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?:
            state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1) ?: 0
        }
    }

    class Factory(
        private val basePageSize: Int,
        private val filmsRepository: FilmsRepository
    ) : () -> FilmsPagingSource {
        override fun invoke(): FilmsPagingSource {
            return FilmsPagingSource(basePageSize, filmsRepository)
        }
    }
}