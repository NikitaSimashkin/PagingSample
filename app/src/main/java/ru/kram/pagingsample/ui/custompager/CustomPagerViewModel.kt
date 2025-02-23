package ru.kram.pagingsample.ui.custompager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.kram.pagingsample.ui.pager.DataSource
import ru.kram.pagingsample.ui.pager.Page
import ru.kram.pagingsample.ui.pager.Pager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kram.pagingsample.data.CatsRepository
import ru.kram.pagingsample.data.db.local.CatLocalDao
import ru.kram.pagingsample.data.db.local.CatLocalEntity
import ru.kram.pagingsample.data.remote.CatsRemoteDataSource
import ru.kram.pagingsample.ui.catlist.model.CatItemData
import ru.kram.pagingsample.ui.catlist.model.CatsScreenState
import timber.log.Timber

class CustomPagerViewModel(
    private val catsRepository: CatsRepository,
    private val catLocalDao: CatLocalDao,
    private val catsRemoteDataSource: CatsRemoteDataSource,
): ViewModel() {

    val screenState = MutableStateFlow(CatsScreenState.EMPTY)

    private val pager = Pager(
        pageSize = PAGE_SIZE,
        maxPagesToKeep = 3,
        threshold = PAGE_SIZE / 2,
        keyByIndex = { index, size -> index / size },
        primaryDataSource = object: DataSource<CatItemData, Int> {
            override suspend fun loadData(key: Int?, loadSize: Int): Page<CatItemData, Int> {
                val key = key ?: 0
                val items = catLocalDao.getCats(limit = loadSize, offset = loadSize * key).map {
                    CatItemData(
                        id = it.id,
                        imageUrl = it.imageUrl,
                        name = it.name,
                        breed = it.breed,
                        age = it.age,
                        createdAt = it.createdAt
                    )
                }
                Timber.d("primary: key=$key, loadSize=$loadSize, items=${items.joinToString(",") { it.name }}")
                return Page(
                    data = items,
                    prevKey = if (key == 0) null else key - 1,
                    nextKey = if (items.size < loadSize) null else key + 1,
                )
            }
        },
        secondaryDataSource = object: DataSource<CatItemData, Int> {
            override suspend fun loadData(key: Int?, loadSize: Int): Page<CatItemData, Int> {
                val key = key ?: 0
                val items = catsRemoteDataSource.getCats(limit = loadSize, offset = loadSize * key).map {
                    CatItemData(
                        id = it.id,
                        imageUrl = it.imageUrl,
                        name = it.name,
                        breed = it.breed,
                        age = it.age,
                        createdAt = it.createdAt
                    )
                }
                catLocalDao.insertAll(
                    items.map {
                        CatLocalEntity(
                            id = it.id,
                            imageUrl = it.imageUrl,
                            name = it.name,
                            breed = it.breed,
                            age = it.age,
                            createdAt = it.createdAt
                        )
                    }
                )
                Timber.d("secondary: key=$key, loadSize=$loadSize, items=${items.joinToString(",") { it.name }}")
                return Page(
                    data = items,
                    prevKey = if (key == 0) null else key - 1,
                    nextKey = if (items.size < loadSize) null else key + 1,
                )
            }
        }
    )

    val cats = pager.pagingFlow

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
            catsRepository.addCat(System.currentTimeMillis())
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
        }
    }

    fun onIndexChanged(index: Int) {
        viewModelScope.launch {
            pager.updateVisibleIndex(index)
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