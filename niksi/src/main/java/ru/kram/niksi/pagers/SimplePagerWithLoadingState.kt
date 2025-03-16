package ru.kram.niksi.pagers

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.kram.niksi.HasLoadingState
import ru.kram.niksi.Pager
import ru.kram.niksi.data.PagedDataSource
import ru.kram.niksi.model.Page
import ru.kram.niksi.model.LoadingState

class SimplePagerWithLoadingState<T, K>(
    private val dataSource: PagedDataSource<T, Int>,
    private val pageSize: Int,
    private val threshold: Int,
    private val maxPagesToKeep: Int,
    private val isSame: (T, K) -> Boolean,
) : Pager<T, K>, HasLoadingState {

    private val loadedPages = sortedMapOf<Int, Page<T, Int>>()

    private val _data = MutableStateFlow<List<T>>(emptyList())
    override val data: StateFlow<List<T>> = _data

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.None)
    override val loadingState: StateFlow<LoadingState> = _loadingState

    private val scope = MainScope()
    private val mutex = Mutex()

    private suspend fun loadPage(page: Int, direction: Direction) {
        if (mutex.withLock { loadedPages.containsKey(page) }) return

        _loadingState.update { current ->
            when (direction) {
                Direction.Start -> when (current) {
                    LoadingState.None -> LoadingState.Start
                    LoadingState.End -> LoadingState.Both
                    LoadingState.Start, LoadingState.Both -> current
                }

                Direction.End -> when (current) {
                    LoadingState.None -> LoadingState.End
                    LoadingState.Start -> LoadingState.Both
                    LoadingState.End, LoadingState.Both -> current
                }
            }
        }

        val pageResult = dataSource.loadData(page, pageSize)
        mutex.withLock { loadedPages[page] = pageResult }
        updateDataFlow()
        mutex.withLock { _loadingState.value = LoadingState.None }
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
            val pagesToReload = mutex.withLock { loadedPages.keys.toList() }
            val jobs = pagesToReload.map { page ->
                async {
                    val pageResult = dataSource.loadData(page, pageSize)
                    mutex.withLock { loadedPages[page] = pageResult }
                }
            }
            jobs.awaitAll()
            updateDataFlow()
            _loadingState.value = LoadingState.None
        }
    }

    override fun onItemVisible(item: K) {
        scope.launch {
            if (mutex.withLock { loadedPages.isEmpty() }) {
                launch { loadPage(0, Direction.End) }
            }
            if (_data.value.isEmpty()) return@launch
            val visibleIndex = _data.value.indexOfFirst { isSame(it, item) }
            if (visibleIndex < 0) return@launch
            val visiblePage = findPageForVisibleIndex(visibleIndex)
            visiblePage?.let { pageKey ->
                val pagesSnapshot = mutex.withLock { loadedPages.toMap() }
                var cumulative = 0
                var targetPage: Page<T, Int>? = null
                for ((_, pageObj) in pagesSnapshot) {
                    if (visibleIndex < cumulative + pageObj.data.size) {
                        targetPage = pageObj
                        break
                    }
                    cumulative += pageObj.data.size
                }
                if (targetPage == null) return@let
                val pageStartIndex = cumulative
                val pageEndIndex = pageStartIndex + targetPage.data.size
                if (visibleIndex - pageStartIndex < threshold) {
                    targetPage.prevKey?.let { prevKey ->
                        if (!mutex.withLock { loadedPages.containsKey(prevKey) }) {
                            launch { loadPage(prevKey, Direction.Start) }
                        }
                    }
                }
                if (pageEndIndex - visibleIndex <= threshold) {
                    targetPage.nextKey?.let { nextKey ->
                        if (!mutex.withLock { loadedPages.containsKey(nextKey) }) {
                            launch { loadPage(nextKey, Direction.End) }
                        }
                    }
                }
                unloadPages(pageKey)
            }
        }
    }

    private enum class Direction {
        Start, End
    }
}