package ru.kram.pagingsample.ui.custompager.filterable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kram.pagerlib.data.PagedDataSource
import ru.kram.pagerlib.model.Page
import ru.kram.pagerlib.pagers.FilterablePager
import ru.kram.pagingsample.data.FilmsRepository
import ru.kram.pagingsample.ui.filmlist.model.FilmItemData
import ru.kram.pagingsample.ui.filmlist.model.InfoBlockData
import timber.log.Timber

class FilterablePagerViewModel(
    private val filmsRepository: FilmsRepository,
): ViewModel() {

    private val filterYear = MutableStateFlow<Int>(2003)

    private val pager = FilterablePager(
        pageSize = PAGE_SIZE,
        maxItemsToKeep = PAGE_SIZE * 3,
        threshold = PAGE_SIZE / 2,
        initialPage = 0,
        primaryDataSource = object: PagedDataSource<FilmItemData, Int> {
            override suspend fun loadData(key: Int, pageSize: Int): Page<FilmItemData, Int> {
                val items = filmsRepository.getFilms(
                    limit = pageSize,
                    offset = pageSize * key,
                    fromNetwork = true
                ).films.mapIndexed { _, it ->
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
        minItemsToLoad = PAGE_SIZE * 3,
    ).apply {
        updateFilterPredicate { item ->
            item.year > 2003
        }
    }

    val filmPagingState = combine(
        pager.data, pager.loadingState, filterYear,
    ) { films, loadingState, filterYear ->
        FilterableFilmsState(
            films = films,
            loadingState = loadingState,
            filterYear = filterYear,
            infoBlockData = InfoBlockData(
                text1Left = "Items in memory: ${films.size}",
                text1Right = "Filter year: $filterYear",
                text2Left = "Loading state: $loadingState",
                text2Right = null,
                text3Left = null,
                text3Right = null,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = FilterableFilmsState.EMPTY,
        started = SharingStarted.Lazily,
    )

    init {
        filterYear.onEach {
            pager.updateFilterPredicate { item ->
                item.year > it
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        Timber.d("onCleared")
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

    fun onItemVisible(item: FilmItemData?) {
        viewModelScope.launch {
            pager.onItemVisible(item)
        }
    }

    fun deleteFilm(film: FilmItemData) {
        viewModelScope.launch {
            filmsRepository.deleteFilm(film.id)
            pager.invalidate()
        }
    }

    fun applyYearFilter(year: Int?) {
        viewModelScope.launch {
            filterYear.update { year ?: 0 }
        }
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}