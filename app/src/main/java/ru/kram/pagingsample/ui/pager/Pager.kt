package ru.kram.pagingsample.ui.pager

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.selects.select
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

interface DataSource<T> {
    suspend fun loadData(page: Int?, loadSize: Int): Page<T>
}

data class Page<T>(
    val data: List<T>,
    val nextPage: Int?,
    val prevPage: Int?,
    val itemsBefore: Int? = null,
    val itemsAfter: Int? = null
)

class Pager<T>(
    private val primaryDataSource: DataSource<T>,
    private val secondaryDataSource: DataSource<T>,
    private val pageSize: Int,
    private val threshold: Int,
    private val maxPagesToKeep: Int,
) {
    private val loadedPagesMap = ConcurrentHashMap<Int, Page<T>>()

    private val _pagingFlow = MutableStateFlow<List<T?>>(emptyList())
    val pagingFlow: StateFlow<List<T?>> get() = _pagingFlow

    private var currentAnchorIndex: Int = 0

    private fun updateFlow() {
        Timber.d("updateFlow")
        val pages = getSortedPages()
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

    private suspend fun loadPage(key: Int?): Page<T> = coroutineScope {
        Timber.d("loadPage: key=$key")
        val primaryDeferred = async { primaryDataSource.loadData(key, pageSize) }
        val secondaryDeferred = async { secondaryDataSource.loadData(key, pageSize) }
        val result = select {
            secondaryDeferred.onAwait { secondaryResult ->
                if (primaryDeferred.isActive) primaryDeferred.cancel()
                key?.let {
                    loadedPagesMap[it] = secondaryResult
                }
                updateFlow()
                secondaryResult
            }
            primaryDeferred.onAwait { primaryResult ->
                key?.let {
                    loadedPagesMap[it] = primaryResult
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
        val targetKey = visibleIndex / pageSize
        val halfWindow = maxPagesToKeep / 2
        val startKey = if (targetKey - halfWindow < 0) 0 else targetKey - halfWindow
        val endKey = targetKey + halfWindow

        loadedPagesMap.keys.filter { it < startKey || it > endKey }.forEach { key ->
            loadedPagesMap.remove(key)
        }
        for (page in startKey..endKey) {
            if (!loadedPagesMap.containsKey(page)) {
                loadPage(page)
            }
        }
        updateFlow()
    }

    private suspend fun loadNextPage() {
        Timber.d("loadNextPage")
        val lastKey = loadedPagesMap.keys.maxOrNull() ?: return
        val lastPage = loadedPagesMap[lastKey] ?: return
        val nextKey = lastPage.nextPage ?: return
        loadPage(nextKey)
    }

    private suspend fun loadPreviousPage() {
        Timber.d("loadPreviousPage")
        val firstKey = loadedPagesMap.keys.minOrNull() ?: return
        val firstPage = loadedPagesMap[firstKey] ?: return
        val prevKey = firstPage.prevPage ?: return
        loadPage(prevKey)
    }

    private fun loadedDataRange(): Pair<Int, Int> {
        Timber.d("loadedDataRange")
        val pages = getSortedPages()
        if (pages.isEmpty()) return 0 to -1
        val start = pages.first().itemsBefore ?: 0
        val totalData = pages.sumOf { it.data.size }
        return start to (start + totalData - 1)
    }

    private fun currentPageIndex(visibleIndex: Int): Int? {
        Timber.d("currentPageIndex: $visibleIndex")
        val pages = getSortedPages()
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
        while (loadedPagesMap.size > maxPagesToKeep) {
            currentIdx = currentPageIndex(visibleIndex) ?: break
            val leftDistance = currentIdx
            val rightDistance = loadedPagesMap.size - currentIdx - 1
            if (leftDistance >= rightDistance) {
                val firstKey = loadedPagesMap.keys.minOrNull() ?: return
                loadedPagesMap.remove(firstKey)
            } else {
                val lastKey = loadedPagesMap.keys.maxOrNull() ?: return
                loadedPagesMap.remove(lastKey)
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
        for (key in loadedPagesMap.keys.sorted()) {
            val refreshed = secondaryDataSource.loadData(key, pageSize)
            loadedPagesMap[key] = refreshed
        }
        updateFlow()
    }

    private fun getSortedPages(): List<Page<T>> {
        return loadedPagesMap.entries.sortedBy { it.key }.map { it.value }
    }
}
