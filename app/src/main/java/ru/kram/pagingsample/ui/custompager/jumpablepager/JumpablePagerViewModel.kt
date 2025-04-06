package ru.kram.pagingsample.ui.custompager.jumpablepager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.kram.pagerlib.data.PagedDataSource
import ru.kram.pagerlib.model.Page
import ru.kram.pagerlib.model.PageWithIndex
import ru.kram.pagerlib.pagers.JumpablePager
import ru.kram.pagingsample.data.CatsRepository
import ru.kram.pagingsample.data.remote.CatsRemoteDataSource
import ru.kram.pagingsample.data.remote.model.CatDTO
import ru.kram.pagingsample.ui.catlist.model.CatItemData
import ru.kram.pagingsample.ui.catlist.model.CatsScreenState
import ru.kram.pagingsample.ui.catlist.model.InfoBlockData
import ru.kram.pagingsample.ui.catlist.model.PagesBlockData
import timber.log.Timber

class JumpablePagerViewModel(
    private val catsRepository: CatsRepository,
    private val catsRemoteDataSource: CatsRemoteDataSource,
): ViewModel() {

    private val pager = JumpablePager<CatDTO, Int>(
        pageSize = PAGE_SIZE,
        maxPagesToKeep = 3,
        threshold = PAGE_SIZE / 2,
        initialPage = 0,
        dataSource = object: PagedDataSource<CatDTO, Int> {
            override suspend fun loadData(key: Int, pageSize: Int): Page<CatDTO, Int> {
                Timber.d("start load page: key=$key")
                val data = catsRemoteDataSource.getCats(limit = pageSize, offset = pageSize * key)
                Timber.d("end load page: key=$key, size=${data.cats.size + (data.itemsBefore ?: 0) + (data.itemsAfter ?: 0)}")
                return Page(
                    data = data.cats,
                    prevKey = if (key == 0) null else key - 1,
                    nextKey = if (data.cats.size < pageSize) null else key + 1,
                    key = key,
                    itemsBefore = data.itemsBefore,
                    itemsAfter = data.itemsAfter,
                )
            }
        },
        pageByItem = { item, pages ->
            val page = item / PAGE_SIZE
            PageWithIndex(page = page, indexInPage = item % PAGE_SIZE)
        }
    )

    val cats = pager.data.map { list ->
        Timber.d("data size=${list.size}")
        list.map {
            if (it == null) {
                return@map null
            }
            CatItemData(
                id = it.id,
                imageUrl = it.imageUrl,
                name = it.name,
                breed = it.breed,
                age = it.age,
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
        cats,
        catsRemoteDataSource.getCatsCount(),
    ) { currentPage, cats, catsCount ->
        val (firstPage, lastPage) = if (catsCount == 0) {
            0 to 0
        } else {
            0 to (catsCount / PAGE_SIZE - if (catsCount % PAGE_SIZE == 0) 1 else 0)
        }
        val itemsInMemory = cats.count { it != null }

        CatsScreenState(
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
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = CatsScreenState.EMPTY,
    )

    override fun onCleared() {
        Timber.d("onCleared")
        pager.destroy()
    }

    fun onAddOneCats() {
        viewModelScope.launch {
            catsRepository.addCat()
            pager.invalidate()
        }
    }

    fun onAdd100Cats() {
        viewModelScope.launch {
            catsRepository.addCats(100)
            pager.invalidate()
        }
    }

    fun clearLocalDb() {
        viewModelScope.launch {
            catsRepository.clearLocal()
            pager.invalidate()
        }
    }

    fun clearUserCats() {
        viewModelScope.launch {
            catsRepository.clearUserCats()
            pager.invalidate()
        }
    }

    fun onIndexVisible(index: Int?) {
        pager.onItemVisible(index)
    }

    fun deleteCat(cat: CatItemData) {
        viewModelScope.launch {
            catsRepository.deleteCat(cat.id)
            pager.invalidate()
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}