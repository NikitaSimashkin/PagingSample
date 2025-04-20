package ru.kram.pagingsample.ui.custompager.simplepager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kram.pagerlib.data.PagedDataSource
import ru.kram.pagerlib.model.Page
import ru.kram.pagerlib.pagers.SimplePager
import ru.kram.pagingsample.data.FilmsRepository
import ru.kram.pagingsample.data.remote.FilmsRemoteDataSource
import ru.kram.pagingsample.ui.filmlist.model.FilmItemData
import ru.kram.pagingsample.ui.filmlist.model.FilmsScreenState
import timber.log.Timber

class SimplePagerViewModel(
    private val filmsRepository: FilmsRepository,
    private val filmsRemoteDataSource: FilmsRemoteDataSource,
): ViewModel() {

    val screenState = MutableStateFlow(FilmsScreenState.EMPTY)

    private val pager = SimplePager(
        pageSize = PAGE_SIZE,
        maxPagesToKeep = 3,
        threshold = PAGE_SIZE / 2,
        initialPage = 0,
        dataSource = object: PagedDataSource<FilmItemData, Int> {
            override suspend fun loadData(key: Int, pageSize: Int): Page<FilmItemData, Int> {
                val items = filmsRemoteDataSource.getFilms(limit = pageSize, offset = pageSize * key).films.mapIndexed { index, it ->
                    FilmItemData(
                        id = it.id,
                        imageUrl = it.imageUrl,
                        name = it.name,
                        year = it.year,
                        createdAt = it.createdAt,
                        number = it.number,
                    )
                }
                Timber.d("primary: key=$key, loadSize=$pageSize, items=${items.joinToString(",") { it.name }}")
                return Page(
                    data = items,
                    prevKey = if (key == 0) null else key - 1,
                    nextKey = if (items.size < pageSize) null else key + 1,
                    key = key,
                )
            }
        },
    )

    val films = pager.data

    override fun onCleared() {
        Timber.d("onCleared")
    }

    fun updateListInfo(itemsInMemory: Int, items: List<FilmItemData?>) {
        screenState.update {
            it.copy(
                infoBlockData = it.infoBlockData.copy(
                    text1Left = "Items in memory: $itemsInMemory"
                )
            )
        }
        Timber.d("updateListInfo: films=${items.joinToString(",") { it?.name.orEmpty() }}, itemsInMemory=$itemsInMemory")
    }

    fun onAddOneFilms() {
        viewModelScope.launch {
            filmsRepository.addFilm()
            pager.invalidate()
        }
    }

    fun onAdd100Films() {
        viewModelScope.launch {
            filmsRepository.addFilms(100)
            pager.invalidate()
        }
    }

    fun clearLocalDb() {
        viewModelScope.launch {
            filmsRepository.clearLocal()
            pager.invalidate()
        }
    }

    fun clearUserFilms() {
        viewModelScope.launch {
            filmsRepository.clearUserFilms()
            pager.invalidate()
        }
    }

    fun onItemVisible(index: Int) {
        viewModelScope.launch {
            pager.onItemVisible(index)
        }
    }

    fun deleteFilm(film: FilmItemData) {
        viewModelScope.launch {
            filmsRepository.deleteFilm(film.id)
            pager.invalidate()
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}