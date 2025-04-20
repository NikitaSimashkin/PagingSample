package ru.kram.pagingsample.ui.custompager.simplepagerloading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kram.pagerlib.data.PagedDataSource
import ru.kram.pagerlib.model.LoadingState
import ru.kram.pagerlib.model.Page
import ru.kram.pagerlib.pagers.SimplePagerWithLoadingState
import ru.kram.pagingsample.data.FilmsRepository
import ru.kram.pagingsample.data.remote.FilmsRemoteDataSource
import ru.kram.pagingsample.domain.FilmDomain
import ru.kram.pagingsample.ui.filmlist.model.FilmItemData
import ru.kram.pagingsample.ui.filmlist.model.FilmsScreenState
import timber.log.Timber

class SimplePagerWithLoadingStateViewModel(
    private val filmsRepository: FilmsRepository,
    private val filmsRemoteDataSource: FilmsRemoteDataSource,
): ViewModel() {

    val screenState = MutableStateFlow(FilmsScreenState.EMPTY)

    private val pager = SimplePagerWithLoadingState<FilmDomain, FilmItemData>(
        pageSize = PAGE_SIZE,
        maxPagesToKeep = 3,
        threshold = PAGE_SIZE / 2,
        initialPage = 0,
        dataSource = object: PagedDataSource<FilmDomain, Int> {
            override suspend fun loadData(key: Int, pageSize: Int): Page<FilmDomain, Int> {
                delay(1000)
                val items = filmsRemoteDataSource.getFilms(limit = pageSize, offset = pageSize * key)
                Timber.d("primary: key=$key, loaded=${items.films.size}")
                return Page(
                    data = items.films,
                    prevKey = if (key == 0) null else key - 1,
                    nextKey = if (items.films.size < pageSize) null else key + 1,
                    key = key,
                )
            }
        },
        isSame = { filmDTO, filmItemData -> filmDTO.id == filmItemData.id }
    )

    private val films = pager.data.map { list ->
        list.map {
            FilmItemData(
                id = it.id,
                imageUrl = it.imageUrl,
                name = it.name,
                year = it.year,
                createdAt = it.createdAt,
                number = it.number,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.Lazily,
    )

    val filmPagingState = combine(
        films, pager.loadingState
    ) { films, loadingState ->
        FilmsWithLoadingState(
            films = films,
            loadingState = loadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = FilmsWithLoadingState(
            films = emptyList(),
            loadingState = LoadingState.None,
        ),
        started = SharingStarted.Lazily,
    )

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
        Timber.d("updateListInfo: films=${items.firstOrNull()?.number}..${items.lastOrNull()?.number}, itemsInMemory=$itemsInMemory")
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

    fun onFilmVisible(film: FilmItemData?) {
        viewModelScope.launch {
            pager.onItemVisible(film)
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