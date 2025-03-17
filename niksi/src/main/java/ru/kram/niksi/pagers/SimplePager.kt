package ru.kram.niksi.pagers

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.kram.niksi.Pager
import ru.kram.niksi.data.PagedDataSource
import ru.kram.niksi.model.Page
import timber.log.Timber

class SimplePager<T>(
    private val dataSource: PagedDataSource<T, Int>,
    private val initialPage: Int,
    private val pageSize: Int,
    private val threshold: Int,
    private val maxPagesToKeep: Int,
) : Pager<T, Int> {

    private val loadedPages = sortedMapOf<Int, Page<T, Int>>()
    private val emptyPages = sortedMapOf<Int, Page<T, Int>>()

    private val _data = MutableStateFlow<List<T>>(emptyList())
    override val data: StateFlow<List<T>> = _data

    private val scope = MainScope()
    private val mutex = Mutex()

    private suspend fun loadPage(page: Int) {
        Timber.d("loadPage: page=$page")
        val pageResult = dataSource.loadData(page, pageSize)
        if (pageResult.data.isEmpty()) {
            mutex.withLock { emptyPages[page] = pageResult }
        }
        mutex.withLock { loadedPages[page] = pageResult }
        updateDataFlow()
    }

    private suspend fun updateDataFlow() {
        val combined = mutex.withLock { loadedPages.values.flatMap { it.data } }
        _data.value = combined
    }

    private suspend fun findPageForVisibleIndex(visibleIndex: Int): Int? {
        var cumulativeCount = 0
        return mutex.withLock {
            for ((page, pageObj) in loadedPages) {
                val nextCount = cumulativeCount + pageObj.data.size
                if (visibleIndex in cumulativeCount until nextCount) return@withLock page
                cumulativeCount = nextCount
            }
            null
        }
    }

    private suspend fun unloadPages(visiblePage: Int) {
        mutex.withLock {
            val halfWindow = maxPagesToKeep / 2
            val minPage = visiblePage - halfWindow
            val maxPage = visiblePage + halfWindow
            loadedPages.keys.filter { it < minPage || it > maxPage }
                .forEach { loadedPages.remove(it) }
        }
        updateDataFlow()
    }

    override fun invalidate(resetTerminalPages: Boolean) {
        scope.launch {
            if (resetTerminalPages) {
                mutex.withLock { emptyPages.clear() }
            }
            val pagesToReload = mutex.withLock { loadedPages.keys.toList() }
            val jobs = pagesToReload.map { page ->
                async {
                    loadPage(page)
                }
            }
            jobs.awaitAll()
            updateDataFlow()
        }
    }

    override fun onItemVisible(item: Int?) {
        val item = item ?: 0
        scope.launch {
            if (mutex.withLock { loadedPages.isEmpty() && !emptyPages.containsKey(item) }) {
                launch { loadPage(initialPage) }
            }
            if (_data.value.isEmpty()) return@launch
            val visiblePage = findPageForVisibleIndex(item)
            visiblePage?.let { pageKey ->
                val pagesSnapshot = mutex.withLock { loadedPages.toMap() }
                var cumulative = 0
                var targetPage: Page<T, Int>? = null
                for ((_, pageObj) in pagesSnapshot) {
                    if (item < cumulative + pageObj.data.size) {
                        targetPage = pageObj
                        break
                    }
                    cumulative += pageObj.data.size
                }
                if (targetPage == null) return@let
                val pageStartIndex = cumulative
                val pageEndIndex = pageStartIndex + targetPage.data.size

                if (item - pageStartIndex < threshold) {
                    targetPage.prevKey?.let { prevKey ->
                        if (!isLoadedOrEmpty(prevKey)) {
                            launch { loadPage(prevKey) }
                        }
                    }
                }
                if (pageEndIndex - item <= threshold) {
                    targetPage.nextKey?.let { nextKey ->
                        if (!isLoadedOrEmpty(nextKey)) {
                            launch { loadPage(nextKey) }
                        }
                    }
                }
                unloadPages(pageKey)
            }
        }
    }

    private suspend fun isLoadedOrEmpty(page: Int): Boolean {
        return mutex.withLock { loadedPages.containsKey(page) || emptyPages.containsKey(page) }
    }
}