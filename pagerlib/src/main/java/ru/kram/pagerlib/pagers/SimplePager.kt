package ru.kram.pagerlib.pagers

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.kram.pagerlib.HasLoadingState
import ru.kram.pagerlib.Pager
import ru.kram.pagerlib.data.PagedDataSource
import ru.kram.pagerlib.model.LoadingState
import ru.kram.pagerlib.model.Page
import timber.log.Timber

class SimplePager<T>(
    private val dataSource: PagedDataSource<T, Int>,
    private val initialPage: Int,
    private val pageSize: Int,
    private val threshold: Int,
    private val maxPagesToKeep: Int,
) : Pager<T, Int>, HasLoadingState {

    private val loadedPages = sortedMapOf<Int, Page<T, Int>>()

    private val emptyPages = mutableSetOf<Int>()
    private val loadingPages = mutableMapOf<Int, Job>()

    private val _data = MutableStateFlow<List<T>>(emptyList())
    override val data: StateFlow<List<T>> = _data

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.None)
    override val loadingState: StateFlow<LoadingState> = _loadingState

    private val scope = MainScope()

    private val dataMutex = Mutex()
    private val processingMutex = Mutex()

    private fun loadPageAsync(page: Int) {
        Timber.d("loadPage: page=$page")

        loadingPages[page] = scope.launch {
            val pageResult = dataSource.loadData(page, pageSize)
            ensureActive()

            dataMutex.withLock {
                loadedPages[page] = pageResult
                loadingPages.remove(page)
                if (pageResult.data.isEmpty()) {
                    emptyPages.add(page)
                }
            }
            updateDataFlow()
        }
    }

    private suspend fun updateDataFlow() {
        _data.value = dataMutex.withLock { loadedPages.values.flatMap { it.data } }
    }

    private suspend fun findPageForVisibleIndex(visibleIndex: Int): Int? {
        var cumulativeCount = 0
        return dataMutex.withLock {
            for ((page, pageObj) in loadedPages) {
                val nextCount = cumulativeCount + pageObj.data.size
                if (visibleIndex in cumulativeCount until nextCount) return@withLock page
                cumulativeCount = nextCount
            }
            null
        }
    }

    private suspend fun unloadPages(visiblePage: Int) {
        dataMutex.withLock {
            val halfWindow = maxPagesToKeep / 2
            val minPage = visiblePage - halfWindow
            val maxPage = visiblePage + halfWindow
            val deletedPages = loadedPages.keys.filter { it < minPage || it > maxPage }
            deletedPages.forEach {
                loadedPages.remove(it)
                loadingPages.remove(it)?.cancel()
            }
            Timber.d("unloadPages: min=$minPage, max=$maxPage")
        }
        updateDataFlow()
    }

    override fun invalidate() {
        scope.launch {
            processingMutex.withLock {
                val pagesToReload = dataMutex.withLock {
                    emptyPages.clear()
                    val keys = loadedPages.keys.toList() + loadingPages.keys
                    loadingPages.forEach { it.value.cancel() }
                    loadingPages.clear()
                    keys
                }
                val jobs = pagesToReload.map { page ->
                    async { loadPageAsync(page) }
                }
                jobs.awaitAll()
                updateDataFlow()
            }
        }
    }

    override fun onItemVisible(item: Int?) {
        scope.launch {
            processingMutex.withLock {
                if ((item == null || loadedPages.isEmpty()) && !needSkipLoading(initialPage)) {
                    launch { loadPageAsync(initialPage) }
                    return@launch
                }
                if (_data.value.isEmpty()) return@launch
                val visibleIndex = item ?: return@launch
                val visiblePage = findPageForVisibleIndex(visibleIndex) ?: return@launch

                loadNewPagesAsync(visibleIndex = visibleIndex, visiblePageKey = visiblePage)
                unloadPages(visiblePage)
            }
        }
    }

    private suspend fun loadNewPagesAsync(visibleIndex: Int, visiblePageKey: Int) {
        val pagesSnapshot = dataMutex.withLock { loadedPages.toMap() }
        val totalCount = pagesSnapshot.values.sumOf { it.data.size }
        val page = pagesSnapshot[visiblePageKey] ?: return

        if (visibleIndex < threshold && page.prevKey != null && !needSkipLoading(page.prevKey)) {
            loadPageAsync(page.prevKey)
        }

        if (totalCount - visibleIndex <= threshold && page.nextKey != null && !needSkipLoading(page.nextKey)) {
            loadPageAsync(page.nextKey)
        }
    }

    override fun destroy() {
        scope.cancel()
        loadedPages.clear()
        emptyPages.clear()
        loadingPages.clear()
    }

    private suspend fun needSkipLoading(page: Int): Boolean {
        return dataMutex.withLock { loadedPages.contains(page) || emptyPages.contains(page) || loadingPages.contains(page) }
    }
}