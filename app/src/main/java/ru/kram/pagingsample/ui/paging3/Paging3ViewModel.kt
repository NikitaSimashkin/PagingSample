package ru.kram.pagingsample.ui.paging3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.ItemSnapshotList
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.room.InvalidationTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kram.pagingsample.data.CatsRepository
import ru.kram.pagingsample.data.db.local.CatLocalDatabase
import ru.kram.pagingsample.data.paging.CatsPagingSource
import ru.kram.pagingsample.data.paging.CatsRemoteMediator
import ru.kram.pagingsample.ui.catlist.model.CatItemData
import ru.kram.pagingsample.ui.catlist.model.CatsScreenState
import timber.log.Timber

@OptIn(ExperimentalPagingApi::class)
class Paging3ViewModel(
    private val catsPagingSourceFactory: CatsPagingSource.Factory,
    private val catLocalDatabase: CatLocalDatabase,
    catsRemoteMediator: CatsRemoteMediator,
    private val catsRepository: CatsRepository,
): ViewModel() {

    val screenState = MutableStateFlow(CatsScreenState.EMPTY)

    private var pagingSource: CatsPagingSource? = null
    private val observer = object : InvalidationTracker.Observer("CatLocalEntity") {
        override fun onInvalidated(tables: Set<String>) {
            pagingSource?.invalidate()
        }
    }

    val catList: Flow<PagingData<CatItemData>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            initialLoadSize = PAGE_SIZE,
            prefetchDistance = PAGE_SIZE / 2,
            maxSize = PAGE_SIZE * 3,
        ),
        pagingSourceFactory = {
            catsPagingSourceFactory().also {
                pagingSource = it
            }
        },
        remoteMediator = catsRemoteMediator,
    ).flow
        .map { flow ->
        flow.map {
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
    }.cachedIn(viewModelScope)

    init {
        observeDatabaseChanges()
    }

    override fun onCleared() {
        Timber.d("onCleared")
        catLocalDatabase.invalidationTracker.removeObserver(observer)
    }

    private fun observeDatabaseChanges() {
        val observer = object : InvalidationTracker.Observer("CatLocalEntity") {
            override fun onInvalidated(tables: Set<String>) {
                pagingSource?.invalidate()
            }
        }
        catLocalDatabase.invalidationTracker.addObserver(observer)
    }

    fun updateListInfo(itemsInMemory: Int, items: ItemSnapshotList<CatItemData>) {
        screenState.update {
            it.copy(
                infoBlockData = it.infoBlockData.copy(
                    text1Left = "Items in memory: $itemsInMemory"
                )
            )
        }
        Timber.d("updateListInfo: cats=${items.joinToString(",") { it?.name ?: "" }}, itemsInMemory=$itemsInMemory")
    }

    fun onAddOneCats() {
        viewModelScope.launch {
            catsRepository.addCat()
        }
    }

    fun onAdd100Cats() {
        viewModelScope.launch {
            catsRepository.addCats(100)
        }
    }

    fun clearLocalDb() {
        viewModelScope.launch {
            catsRepository.clearLocal()
        }
    }

    fun deleteCat(id: String) {
        viewModelScope.launch {
            catsRepository.deleteCat(id)
            pagingSource?.invalidate()
        }
    }

    fun clearUserCats() {
        viewModelScope.launch {
            catsRepository.clearUserCats()
        }
    }

    companion object {
        const val PAGE_SIZE = 30
    }
}