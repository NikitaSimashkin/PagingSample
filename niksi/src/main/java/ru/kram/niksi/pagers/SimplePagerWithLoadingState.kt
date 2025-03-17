package ru.kram.niksi.pagers

import kotlinx.coroutines.Job
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
import ru.kram.niksi.model.LoadingState
import ru.kram.niksi.model.Page
import timber.log.Timber

class SimplePagerWithLoadingState<T, K>(
    private val dataSource: PagedDataSource<T, Int>,
    private val initialPage: Int,
    private val pageSize: Int,
    private val threshold: Int,
    private val maxPagesToKeep: Int,
    private val isSame: (T, K) -> Boolean,
) : Pager<T, K>, HasLoadingState {

    private val loadedPages = sortedMapOf<Int, Page<T, Int>>()
    private val emptyPages = sortedMapOf<Int, Page<T, Int>>()

    private val loadingPages = mutableSetOf<Int>()

    private val _data = MutableStateFlow<List<T>>(emptyList())
    override val data: StateFlow<List<T>> = _data

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.None)
    override val loadingState: StateFlow<LoadingState> = _loadingState

    private val scope = MainScope()
    private val dataMutex = Mutex()

    private var currentProcessingPage: Int? = null
    private var currentProcessingJob: Job? = null

    private suspend fun loadPage(page: Int, direction: Direction?) {
        if (dataMutex.withLock { loadingPages.contains(page) }) return

        dataMutex.withLock { loadingPages.add(page) }

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

                null -> LoadingState.Start
            }
        }

        Timber.d("loadPage: page=$page, direction=$direction")
        val pageResult = dataSource.loadData(page, pageSize)
        if (pageResult.data.isEmpty()) {
            dataMutex.withLock { emptyPages[page] = pageResult }
        }
        dataMutex.withLock { loadedPages[page] = pageResult }
        updateDataFlow()
        dataMutex.withLock {
            _loadingState.value = LoadingState.None
            loadingPages.remove(page)
        }
    }

    private suspend fun updateDataFlow() {
        val combined = dataMutex.withLock { loadedPages.values.flatMap { it.data } }
        _data.value = combined
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
            loadedPages.keys.filter { it < minPage || it > maxPage }
                .forEach { loadedPages.remove(it) }
            Timber.d("unloadPages: min=$minPage, max=$maxPage")
        }
        updateDataFlow()
    }

    override fun invalidate(resetTerminalPages: Boolean) {
        scope.launch {
            if (resetTerminalPages) {
                dataMutex.withLock { emptyPages.clear() }
            }
            val pagesToReload = dataMutex.withLock { loadedPages.keys.toList() }
            val jobs = pagesToReload.map { page ->
                async {
                    loadPage(page, null)
                }
            }
            jobs.awaitAll()
            updateDataFlow()
        }
    }

    override fun onItemVisible(item: K?) {
        scope.launch {
            if (dataMutex.withLock { loadedPages.isEmpty() && !emptyPages.containsKey(initialPage) } || item == null) {
                launch { loadPage(initialPage, null) }
                return@launch
            }
            if (_data.value.isEmpty()) return@launch
            val visibleIndex = _data.value.indexOfFirst { isSame(it, item) }
            if (visibleIndex < 0) return@launch
            val visiblePage = findPageForVisibleIndex(visibleIndex)
            if (visiblePage == null || visiblePage == currentProcessingPage) return@launch

            currentProcessingJob?.cancel()
            currentProcessingPage = visiblePage

            Timber.d("onItemVisible: visibleIndex=$visibleIndex, visiblePage=$visiblePage")

            currentProcessingJob = scope.launch {
                val pagesSnapshot = dataMutex.withLock { loadedPages.toMap() }
                var cumulative = 0
                var targetPage: Page<T, Int>? = null
                for ((_, pageObj) in pagesSnapshot) {
                    if (visibleIndex < cumulative + pageObj.data.size) {
                        targetPage = pageObj
                        break
                    }
                    cumulative += pageObj.data.size
                }
                if (targetPage == null) return@launch
                val pageStartIndex = cumulative
                val pageEndIndex = pageStartIndex + targetPage.data.size

                if (visibleIndex - pageStartIndex < threshold) {
                    targetPage.prevKey?.let { prevKey ->
                        if (!needSkipLoading(prevKey)) {
                            launch { loadPage(prevKey, Direction.Start) }
                        }
                    }
                }
                if (pageEndIndex - visibleIndex <= threshold) {
                    targetPage.nextKey?.let { nextKey ->
                        if (!needSkipLoading(nextKey)) {
                            launch { loadPage(nextKey, Direction.End) }
                        }
                    }
                }
                unloadPages(visiblePage)

                currentProcessingPage = null
                currentProcessingJob = null
            }
        }
    }

    private suspend fun needSkipLoading(page: Int): Boolean {
        return dataMutex.withLock { loadedPages.containsKey(page) || emptyPages.containsKey(page) || loadingPages.contains(page) }
    }

    private enum class Direction {
        Start, End
    }
}