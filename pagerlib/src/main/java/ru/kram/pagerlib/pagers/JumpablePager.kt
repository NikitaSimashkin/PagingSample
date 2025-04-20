package ru.kram.pagerlib.pagers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.kram.pagerlib.Pager
import ru.kram.pagerlib.data.PagedDataSource
import ru.kram.pagerlib.model.Page
import ru.kram.pagerlib.model.PageWithIndex
import timber.log.Timber
import java.util.concurrent.LinkedBlockingDeque
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

class JumpablePager<T, K>(
    private val primaryDataSource: PagedDataSource<T, Int>,
    private val initialPage: Int,
    private val pageSize: Int,
    private val threshold: Int,
    private val maxPagesToKeep: Int,
    private val pageByItem: (K, Collection<Page<T, Int>>) -> PageWithIndex<Int>,
) : Pager<T, K> {

    private val loadedPages = sortedMapOf<Int, Page<T, Int>>()
    private val emptyPages = mutableSetOf<Int>()
    private val loadingPages = mutableMapOf<Int, Job>()

    private val _data = MutableStateFlow<List<T?>>(emptyList())
    override val data: StateFlow<List<T?>> = _data

    private val scope = MainScope()
    private val dataMutex = Mutex()

    private val actionDeque = LinkedBlockingDeque<Action>(Int.MAX_VALUE)

    private val lastVisibleItem = MutableStateFlow<K?>(null)
    private val currentVisiblePageWithIndex = MutableStateFlow<PageWithIndex<Int>?>(null)

    val currentPage = currentVisiblePageWithIndex.map {
        Timber.d("currentPage: $it")
        it?.page
    }.stateIn(
        scope = scope,
        initialValue = null,
        started = SharingStarted.Lazily
    )

    init {
        lastVisibleItem.onEach { item ->
            Timber.d("lastVisibleItem: $item")
            val pageWithIndex = if (item != null) pageByItem(item, loadedPages.values) else null
            currentVisiblePageWithIndex.value = pageWithIndex
            actionDeque.addLast(Action.CheckNeedLoad)
        }.launchIn(scope)

        scope.launch {
            while (true) {
                val action = withContext(Dispatchers.IO) { actionDeque.takeLast() }
                Timber.d("action=$action, state=${currentVisiblePageWithIndex.value}  loadedPages=${loadedPages.keys}, loadingPages=${loadingPages.keys}")
                val pageWithIndex = currentVisiblePageWithIndex.value ?: continue
                val visiblePageKey = pageWithIndex.page
                val visibleIndex = pageWithIndex.indexInPage

                when (action) {
                    is Action.LoadPage -> processLoadPage(action, currentVisiblePageKey = visiblePageKey, indexInPage = visibleIndex)
                    is Action.RemovePages -> processRemovePages()
                    is Action.UpdateDataFlow -> processUpdateDataFlow()
                    is Action.Invalidate -> processInvalidate(visiblePageKey = visiblePageKey)
                    is Action.CheckNeedLoad -> processCheckNeedLoad()
                }
            }
        }
    }

    private suspend fun processCheckNeedLoad() {
        val pageWithIndex = currentVisiblePageWithIndex.value

        if (dataMutex.withLock { loadedPages.isEmpty() && !emptyPages.contains(initialPage) && !loadingPages.contains(initialPage) && pageWithIndex?.indexInPage == initialPage }) {
            actionDeque.addLast(Action.LoadPage(initialPage))
        } else if (pageWithIndex?.page != null && !needSkipLoading(pageWithIndex.page)) {
            actionDeque.addLast(Action.LoadPage(pageWithIndex.page))
        }

        pageWithIndex ?: return
        val currentVisiblePage = dataMutex.withLock { loadedPages[pageWithIndex.page] } ?: return

        if (currentVisiblePage.prevKey != null && pageWithIndex.indexInPage < threshold) {
            actionDeque.addLast(Action.LoadPage(currentVisiblePage.prevKey))
        }
        if (currentVisiblePage.nextKey != null && currentVisiblePage.data.size - pageWithIndex.indexInPage <= threshold) {
            actionDeque.addLast(Action.LoadPage(currentVisiblePage.nextKey))
        }
    }

    private suspend fun processInvalidate(visiblePageKey: Int) {
        dataMutex.withLock {
            loadedPages.clear()
            loadingPages.forEach { (_, job) -> job.cancel() }
            loadingPages.clear()
            emptyPages.clear()

            Timber.d("processInvalidate")
        }
        loadMultiplePages(visiblePageKey)
    }

    private suspend fun processLoadPage(
        action: Action.LoadPage,
        currentVisiblePageKey: Int,
        indexInPage: Int,
    ) {
        if (currentVisiblePageKey == action.key) {
            if (!needSkipLoading(action.key)) {
                loadPageAsync(action.key)
            }
        } else {
            val currentVisiblePage = loadedPages[currentVisiblePageKey] ?: return

            if (currentVisiblePage.prevKey == action.key && indexInPage < threshold && !needSkipLoading(currentVisiblePage.prevKey)) {
                loadPageAsync(currentVisiblePage.prevKey)
            }
            if (currentVisiblePage.nextKey == action.key && currentVisiblePage.data.size - indexInPage <= threshold && !needSkipLoading(currentVisiblePage.nextKey)) {
                loadPageAsync(currentVisiblePage.nextKey)
            }
        }
    }

    private suspend fun loadMultiplePages(currentVisiblePageKey: Int) {
        coroutineScope {
            val page = loadPage(currentVisiblePageKey)
            val adjacentKeys = listOfNotNull(page.prevKey, page.nextKey)
            adjacentKeys.map { key -> async { loadPage(key) } }.awaitAll()
            onNewPageLoaded(page)
        }
    }

    private suspend fun processRemovePages() {
        if (loadedPages.size > maxPagesToKeep) {
            dataMutex.withLock {
                val currentVisiblePageKey =
                    currentVisiblePageWithIndex.value?.page ?: return@withLock
                val excessCount = loadedPages.size - maxPagesToKeep

                val keysToRemove = loadedPages.keys
                    .sortedByDescending { abs(it - currentVisiblePageKey) }
                    .take(excessCount)
                keysToRemove.forEach { key ->
                    Timber.d("processRemovePages: removed page key=$key, currentVisiblePageKey=$currentVisiblePageKey")
                    loadedPages.remove(key)
                    loadingPages.remove(key)?.cancel()
                }
            }
        }
    }

    private suspend fun loadPage(page: Int): Page<T, Int> {
        Timber.d("loadPage: page=$page")

        val pageResult = primaryDataSource.loadData(page, pageSize)

        dataMutex.withLock {
            loadedPages[page] = pageResult
            if (pageResult.data.isEmpty()) {
                emptyPages.add(page)
            }
        }

        return pageResult
    }

    private fun loadPageAsync(page: Int): Job {
        Timber.d("loadPageAsync: page=$page")
        loadingPages[page]?.cancel()
        val loadJob = scope.launch {
            ensureActive()
            val pageResult = loadPage(page)
            loadingPages.remove(page)
            onNewPageLoaded(pageResult)
        }
        Timber.d("loadPageAsync: $page, finished")
        loadingPages[page] = loadJob
        return loadJob
    }

    private suspend fun processUpdateDataFlow() {
        dataMutex.withLock {
            _data.value = buildList {
                if (loadedPages.isEmpty()) return@buildList

                val pagesList = loadedPages.values.toList()
                pagesList.map {
                    "${it.key} (${it.itemsBefore})(${it.data.size})(${it.itemsAfter})"
                }.joinToString().apply {
                    Timber.d("updateDataFlow: $this")
                }
                val firstPage = pagesList.first()
                repeat(firstPage.itemsBefore ?: 0) { add(null) }
                addAll(firstPage.data)
                pagesList.zipWithNext { current, next ->
                    if (current.nextKey != next.key) {
                        val gap =
                            (current.itemsAfter ?: 0) - ((next.itemsAfter ?: 0) + next.data.size)
                        if (gap > 0) {
                            repeat(gap) { add(null) }
                        }
                    }
                    addAll(next.data)
                }
                val lastPage = pagesList.last()
                repeat(lastPage.itemsAfter ?: 0) { add(null) }
            }
        }
    }

    private suspend fun onNewPageLoaded(page: Page<T, Int>) {
        coroutineContext.ensureActive()

        actionDeque.addLast(Action.UpdateDataFlow)
        actionDeque.addLast(Action.RemovePages)

        val currentState = currentVisiblePageWithIndex.value
        if (page.key == currentState?.page) {
            actionDeque.addLast(Action.CheckNeedLoad)
        }
    }

    override fun invalidate() {
        actionDeque.addLast(Action.Invalidate())
    }

    override fun onItemVisible(item: K?) {
        lastVisibleItem.value = item
    }

    override fun destroy() {
        scope.cancel()
        loadedPages.clear()
        emptyPages.clear()
        loadingPages.clear()
    }

    private suspend fun needSkipLoading(key: Int): Boolean {
        return dataMutex.withLock {
            (loadedPages.contains(key) || emptyPages.contains(key) || loadingPages.contains(key))
        }
    }

    sealed class Action {
        data class LoadPage(val key: Int) : Action()
        data object RemovePages : Action()
        data object UpdateDataFlow : Action()
        class Invalidate : Action()
        data object CheckNeedLoad : Action()
    }
}
