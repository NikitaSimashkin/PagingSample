package ru.kram.pagingsample.ui.pager

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.selects.select
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

interface DataSource<T, K> {
    suspend fun loadData(key: K?, loadSize: Int): Page<T, K>
}

data class Page<T, K>(
    val data: List<T>,
    val prevKey: K?,
    val nextKey: K?,
    val itemsBefore: Int? = null,
    val itemsAfter: Int? = null
)

class Pager<T, K>(
    private val primaryDataSource: DataSource<T, K>,
    private val secondaryDataSource: DataSource<T, K>,
    private val pageSize: Int,
    private val threshold: Int,
    private val keyByIndex: suspend (visibleIndex: Int, pageSize: Int) -> K?,
    private val maxPagesToKeep: Int,
) {
    private val loadedPagesMap = ConcurrentHashMap<K, Page<T, K>>()
    private val loadedPagesOrder = CopyOnWriteArrayList<K>()

    private val _pagingFlow = MutableStateFlow<List<T?>>(emptyList())
    val pagingFlow: StateFlow<List<T?>> get() = _pagingFlow

    private var currentAnchorIndex: Int = 0

    private fun updateFlow() {
        Timber.d("updateFlow")
        val pages = loadedPagesOrder.mapNotNull { key -> loadedPagesMap[key] }
        if (pages.isEmpty()) {
            _pagingFlow.value = emptyList()
            return
        }
        val itemsBefore = pages.first().itemsBefore
        val dataItems = pages.flatMap { it.data }
        val itemsAfter = pages.last().itemsAfter
        _pagingFlow.value = List(itemsBefore ?: 0) { null } + dataItems + List(itemsAfter ?: 0) { null }
    }

    private suspend fun ensureInitialPage() {
        Timber.d("ensureInitialPage")
        if (loadedPagesMap.isEmpty()) {
            jumpToGlobalIndex(currentAnchorIndex)
        }
    }

    private suspend fun loadPage(key: K?, prepend: Boolean = false): Page<T, K> = coroutineScope {
        Timber.d("loadPage: key=$key, prepend=$prepend")
        val primaryDeferred = async { primaryDataSource.loadData(key, pageSize) }
        val secondaryDeferred = async { secondaryDataSource.loadData(key, pageSize) }
        val result = select {
            secondaryDeferred.onAwait { secondaryResult ->
                if (primaryDeferred.isActive) primaryDeferred.cancel()
                key?.let {
                    loadedPagesMap[it] = secondaryResult
                    if (!loadedPagesOrder.contains(it)) {
                        if (prepend) loadedPagesOrder.add(0, it) else loadedPagesOrder.add(it)
                    }
                }
                updateFlow()
                secondaryResult
            }
            primaryDeferred.onAwait { primaryResult ->
                key?.let {
                    loadedPagesMap[it] = primaryResult
                    if (!loadedPagesOrder.contains(it)) {
                        if (prepend) loadedPagesOrder.add(0, it) else loadedPagesOrder.add(it)
                    }
                }
                updateFlow()
                val secondaryResult = secondaryDeferred.await()
                key?.let { loadedPagesMap[it] = secondaryResult }
                updateFlow()
                secondaryResult
            }
        }
        result
    }

    private suspend fun jumpToGlobalIndex(visibleIndex: Int) {
        Timber.d("jumpToGlobalIndex: $visibleIndex")
        val key = keyByIndex(visibleIndex, pageSize)
        loadedPagesMap.clear()
        loadedPagesOrder.clear()
        loadPage(key, prepend = false)
    }

    private suspend fun loadNextPage() {
        Timber.d("loadNextPage")
        val lastKey = loadedPagesOrder.lastOrNull() ?: return
        val lastPage = loadedPagesMap[lastKey] ?: return
        val nextKey = lastPage.nextKey ?: return
        loadPage(nextKey, prepend = false)
    }

    private suspend fun loadPreviousPage() {
        Timber.d("loadPreviousPage")
        val firstKey = loadedPagesOrder.firstOrNull() ?: return
        val firstPage = loadedPagesMap[firstKey] ?: return
        val prevKey = firstPage.prevKey ?: return
        loadPage(prevKey, prepend = true)
    }

    private fun loadedDataRange(): Pair<Int, Int> {
        Timber.d("loadedDataRange")
        val pages = loadedPagesOrder.mapNotNull { loadedPagesMap[it] }
        if (pages.isEmpty()) return 0 to -1
        val start = pages.first().itemsBefore ?: 0
        val totalData = pages.sumOf { it.data.size }
        return start to (start + totalData - 1)
    }

    private fun currentPageIndex(visibleIndex: Int): Int? {
        Timber.d("currentPageIndex: $visibleIndex")
        val pages = loadedPagesOrder.mapNotNull { loadedPagesMap[it] }
        if (pages.isEmpty()) return null
        var offset = visibleIndex - (pages.first().itemsBefore ?: 0)
        if (offset < 0) return null
        for ((i, page) in pages.withIndex()) {
            if (offset < page.data.size) return i
            offset -= page.data.size
        }
        return null
    }

    private fun unloadFarPages(visibleIndex: Int) {
        var currentIdx: Int
        while (loadedPagesOrder.size > maxPagesToKeep) {
            currentIdx = currentPageIndex(visibleIndex) ?: break
            val leftDistance = currentIdx
            val rightDistance = loadedPagesOrder.size - currentIdx - 1
            if (leftDistance >= rightDistance) {
                val key = loadedPagesOrder.first()
                loadedPagesOrder.removeAt(0)
                loadedPagesMap.remove(key)
            } else {
                val key = loadedPagesOrder.last()
                loadedPagesOrder.removeAt(loadedPagesOrder.size - 1)
                loadedPagesMap.remove(key)
            }
        }
        updateFlow()
    }

    suspend fun updateVisibleIndex(visibleIndex: Int) {
        currentAnchorIndex = visibleIndex
        ensureInitialPage()
        updateFlow()
        val (firstData, lastData) = loadedDataRange()

        val gapFromTop = visibleIndex - firstData
        val gapFromBottom = lastData - visibleIndex
        when {
            visibleIndex < firstData -> {
                val gap = firstData - visibleIndex
                if (gap <= pageSize) {
                    loadPreviousPage()
                } else {
                    jumpToGlobalIndex(visibleIndex)
                }
            }
            visibleIndex > lastData -> {
                val gap = visibleIndex - lastData
                if (gap <= pageSize) {
                    loadNextPage()
                } else {
                    jumpToGlobalIndex(visibleIndex)
                }
            }
            gapFromTop <= threshold -> loadPreviousPage()
            gapFromBottom <= threshold -> loadNextPage()
            else -> unloadFarPages(visibleIndex)
        }
    }

    suspend fun invalidate() {
        Timber.d("invalidate")
        for (key in loadedPagesOrder) {
            val refreshed = secondaryDataSource.loadData(key, pageSize)
            loadedPagesMap[key] = refreshed
        }
        updateFlow()
    }
}
