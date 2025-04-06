package ru.kram.pagingsample.ui.custompager.simplepager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kram.pagerlib.data.PagedDataSource
import ru.kram.pagerlib.model.Page
import ru.kram.pagerlib.pagers.SimplePager
import ru.kram.pagingsample.data.CatsRepository
import ru.kram.pagingsample.data.remote.CatsRemoteDataSource
import ru.kram.pagingsample.ui.catlist.model.CatItemData
import ru.kram.pagingsample.ui.catlist.model.CatsScreenState
import timber.log.Timber

class SimplePagerViewModel(
    private val catsRepository: CatsRepository,
    private val catsRemoteDataSource: CatsRemoteDataSource,
): ViewModel() {

    val screenState = MutableStateFlow(CatsScreenState.EMPTY)

    private val pager = SimplePager(
        pageSize = PAGE_SIZE,
        maxPagesToKeep = 3,
        threshold = PAGE_SIZE / 2,
        initialPage = 0,
        dataSource = object: PagedDataSource<CatItemData, Int> {
            override suspend fun loadData(key: Int, pageSize: Int): Page<CatItemData, Int> {
                val items = catsRemoteDataSource.getCats(limit = pageSize, offset = pageSize * key).cats.mapIndexed { index, it ->
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

    val cats = pager.data

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
        Timber.d("updateListInfo: cats=${items.joinToString(",") { it?.name.orEmpty() }}, itemsInMemory=$itemsInMemory")
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

    fun onItemVisible(index: Int) {
        viewModelScope.launch {
            pager.onItemVisible(index)
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