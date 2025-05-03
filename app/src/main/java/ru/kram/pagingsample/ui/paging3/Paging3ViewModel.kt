package ru.kram.pagingsample.ui.paging3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.ItemSnapshotList
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import androidx.room.InvalidationTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kram.pagingsample.data.db.local.FilmLocalDatabase
import ru.kram.pagingsample.data.paging.FilmsPagingSource
import ru.kram.pagingsample.data.paging.FilmsRemoteMediator
import ru.kram.pagingsample.domain.FilmsRepository
import ru.kram.pagingsample.ui.filmlist.model.FilmItemData
import ru.kram.pagingsample.ui.filmlist.model.InfoBlockData
import timber.log.Timber

@OptIn(ExperimentalPagingApi::class)
class Paging3ViewModel(
    private val filmsPagingSourceFactory: FilmsPagingSource.Factory,
    private val filmLocalDatabase: FilmLocalDatabase,
    filmsRemoteMediator: FilmsRemoteMediator,
    private val filmsRepository: FilmsRepository,
) : ViewModel() {

    val screenState = MutableStateFlow(InfoBlockData.EMPTY)

    private var pagingSource: FilmsPagingSource? = null
    private val observer = object : InvalidationTracker.Observer("FilmLocalEntity") {
        override fun onInvalidated(tables: Set<String>) {
            pagingSource?.invalidate()
        }
    }

    val filmsList: Flow<PagingData<FilmItemData>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                maxSize = PAGE_SIZE * 3,
            ),
            pagingSourceFactory = {
                filmsPagingSourceFactory()
            },
            remoteMediator = filmsRemoteMediator,
        ).flow.map { pagingData ->
            pagingData.map {
                FilmItemData(
                    id = it.id,
                    imageUrl = it.imageUrl,
                    name = it.name,
                    year = it.year,
                    createdAt = it.createdAt,
                    number = it.number,
                )
            }.filter {
                it.year > 2003
            }
        }.cachedIn(viewModelScope)

    init {
        observeDatabaseChanges()
    }

    override fun onCleared() {
        Timber.d("onCleared")
        filmLocalDatabase.invalidationTracker.removeObserver(observer)
    }

    private fun observeDatabaseChanges() {
        val observer = object : InvalidationTracker.Observer("FilmLocalEntity") {
            override fun onInvalidated(tables: Set<String>) {
                pagingSource?.invalidate()
            }
        }
        filmLocalDatabase.invalidationTracker.addObserver(observer)
    }

    fun updateListInfo(itemsInMemory: Int, items: ItemSnapshotList<FilmItemData>) {
        screenState.update {
            it.copy(
                text1Left = "Items in memory: $itemsInMemory"
            )
        }
        Timber.d("updateListInfo: films=${items.joinToString(",") { it?.name ?: "" }}, itemsInMemory=$itemsInMemory")
    }

    fun onAddOneFilms() {
        viewModelScope.launch {
            filmsRepository.addFilm()
        }
    }

    fun onAdd100Films() {
        viewModelScope.launch {
            filmsRepository.addFilms(100)
        }
    }

    fun clearLocalDb() {
        viewModelScope.launch {
            filmsRepository.clearLocal()
        }
    }

    fun deleteFilm(id: String) {
        viewModelScope.launch {
            filmsRepository.deleteFilm(id)
            pagingSource?.invalidate()
        }
    }

    fun clearUserFilms() {
        viewModelScope.launch {
            filmsRepository.clearUserFilms()
        }
    }

    companion object {
        const val PAGE_SIZE = 30
    }
}
