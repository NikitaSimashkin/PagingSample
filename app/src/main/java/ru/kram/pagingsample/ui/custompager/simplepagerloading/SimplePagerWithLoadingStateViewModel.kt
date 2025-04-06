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
import ru.kram.pagingsample.data.CatsRepository
import ru.kram.pagingsample.data.remote.CatsRemoteDataSource
import ru.kram.pagingsample.data.remote.model.CatDTO
import ru.kram.pagingsample.ui.catlist.model.CatItemData
import ru.kram.pagingsample.ui.catlist.model.CatsScreenState
import timber.log.Timber

class SimplePagerWithLoadingStateViewModel(
    private val catsRepository: CatsRepository,
    private val catsRemoteDataSource: CatsRemoteDataSource,
): ViewModel() {

    val screenState = MutableStateFlow(CatsScreenState.EMPTY)

    private val pager = SimplePagerWithLoadingState<CatDTO, CatItemData>(
        pageSize = PAGE_SIZE,
        maxPagesToKeep = 3,
        threshold = PAGE_SIZE / 2,
        initialPage = 0,
        dataSource = object: PagedDataSource<CatDTO, Int> {
            override suspend fun loadData(key: Int, pageSize: Int): Page<CatDTO, Int> {
                delay(1000)
                val items = catsRemoteDataSource.getCats(limit = pageSize, offset = pageSize * key)
                Timber.d("primary: key=$key, loaded=${items.cats.size}")
                return Page(
                    data = items.cats,
                    prevKey = if (key == 0) null else key - 1,
                    nextKey = if (items.cats.size < pageSize) null else key + 1,
                    key = key,
                )
            }
        },
        isSame = { catDTO, catItemData -> catDTO.id == catItemData.id }
    )

    private val cats = pager.data.map { list ->
        list.map {
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

    val catPagingState = combine(
        cats, pager.loadingState
    ) { cats, loadingState ->
        CatsWithLoadingState(
            cats = cats,
            loadingState = loadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = CatsWithLoadingState(
            cats = emptyList(),
            loadingState = LoadingState.None,
        ),
        started = SharingStarted.Lazily,
    )

    override fun onCleared() {
        Timber.d("onCleared")
    }

    fun updateListInfo(itemsInMemory: Int, items: List<CatItemData?>) {
        screenState.update {
            it.copy(
                infoBlockData = it.infoBlockData.copy(
                    text1Left = "Items in memory: $itemsInMemory"
                )
            )
        }
        Timber.d("updateListInfo: cats=${items.firstOrNull()?.number}..${items.lastOrNull()?.number}, itemsInMemory=$itemsInMemory")
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

    fun onCatVisible(cat: CatItemData?) {
        viewModelScope.launch {
            pager.onItemVisible(cat)
        }
    }

    fun deleteCat(cat: CatItemData) {
        viewModelScope.launch {
            catsRepository.deleteCat(cat.id)
            pager.invalidate()
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}