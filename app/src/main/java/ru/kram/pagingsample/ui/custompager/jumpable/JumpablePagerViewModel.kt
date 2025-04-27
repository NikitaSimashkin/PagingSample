package ru.kram.pagingsample.ui.custompager.jumpable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.kram.pagerlib.data.PagedDataSource
import ru.kram.pagerlib.model.Page
import ru.kram.pagerlib.model.PageWithIndex
import ru.kram.pagerlib.pagers.JumpablePager
import ru.kram.pagingsample.data.FilmsRepository
import ru.kram.pagingsample.domain.FilmDomain
import ru.kram.pagingsample.ui.filmlist.model.FilmItemData
import ru.kram.pagingsample.ui.filmlist.model.InfoBlockData
import ru.kram.pagingsample.ui.filmlist.model.PagesBlockData
import timber.log.Timber

class JumpablePagerViewModel(
    private val filmsRepository: FilmsRepository,
): ViewModel() {

    private val pager = JumpablePager<FilmDomain, Int>(
        pageSize = PAGE_SIZE,
        maxPagesToKeep = 3,
        threshold = PAGE_SIZE / 2,
        initialPage = 0,
        primaryDataSource = object: PagedDataSource<FilmDomain, Int> {
            override suspend fun loadData(key: Int, pageSize: Int): Page<FilmDomain, Int> {
                Timber.d("primary start load page: key=$key")
                val offset = key * pageSize
                val data = filmsRepository.getFilms(limit = pageSize, offset = pageSize * key, fromNetwork = true)
                val itemsBefore = offset.coerceAtMost(data.totalCount)
                val itemsAfter = (data.totalCount - (offset + data.films.size)).coerceAtLeast(0)
                return Page(
                    data = data.films,
                    prevKey = if (key == 0) null else key - 1,
                    nextKey = if (data.films.size < pageSize) null else key + 1,
                    key = key,
                    itemsBefore = itemsBefore,
                    itemsAfter = itemsAfter,
                )
            }
        },
        pageByItem = { item, pages ->
            val page = item / PAGE_SIZE
            PageWithIndex(page = page, indexInPage = item % PAGE_SIZE)
        }
    )

    private val films = pager.data.map { list ->
        Timber.d("data size=${list.size}")
        list.map {
            if (it == null) {
                return@map null
            }
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

    val screenState = combine(
        pager.currentPage,
        films,
        filmsRepository.observeFilmsCount().onEach {
            Timber.d("observeFilmsCount: $it")
        },
    ) { currentPage, films, filmsCount ->
        val (firstPage, lastPage) = if (filmsCount == 0) {
            0 to 0
        } else {
            0 to (filmsCount / PAGE_SIZE - if (filmsCount % PAGE_SIZE == 0) 1 else 0)
        }
        val itemsInMemory = films.count { it != null }

        JumpableScreenState(
            infoBlockData = InfoBlockData(
                text1Left = "Items in memory: $itemsInMemory",
                text1Right = null,
                text2Left = null,
                text2Right = null,
                text3Left = null,
                text3Right = null,
            ),
            pagesBlockData = PagesBlockData(
                currentPage = (currentPage ?: -1),
                firstPage = firstPage,
                lastPage = lastPage,
            ),
            films = films,
            page = currentPage,
        )
    }.onStart {
        viewModelScope.launch {
            filmsRepository.updateTotalCount()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = JumpableScreenState.EMPTY,
    )

    override fun onCleared() {
        Timber.d("onCleared")
        pager.destroy()
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

    fun onIndexVisible(index: Int?) {
        pager.onItemVisible(index)
    }

    fun deleteFilm(film: FilmItemData) {
        viewModelScope.launch {
            filmsRepository.deleteFilm(film.id)
            pager.invalidate()
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}