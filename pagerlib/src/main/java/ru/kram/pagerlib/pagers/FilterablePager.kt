package ru.kram.pagerlib.pagers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.kram.pagerlib.HasLoadingState
import ru.kram.pagerlib.Pager
import ru.kram.pagerlib.data.PagedDataSource
import ru.kram.pagerlib.model.LoadingState
import ru.kram.pagerlib.model.Page
import ru.kram.pagerlib.util.PageHelper
import timber.log.Timber
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

class FilterablePager<T>(
    private val primaryDataSource: PagedDataSource<T, Int>,
    private val initialPage: Int,
    private val pageSize: Int,
    private val threshold: Int,
    private val maxItemsToKeep: Int,
    private val minItemsToLoad: Int,
) : Pager<T, T>, HasLoadingState {

    private val pageHelper = PageHelper<T, Int>()

    private val loadingPages = mutableMapOf<Int, Job>()

    private val _data = MutableStateFlow<List<T?>>(emptyList())
    override val data: StateFlow<List<T?>> = _data

    private val scope = MainScope()
    private val dataMutex = Mutex()

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.None)
    override val loadingState: StateFlow<LoadingState> = _loadingState

    private val actionChannel = Channel<Action>(Channel.UNLIMITED)

    private val lastVisibleItem = MutableStateFlow<T?>(null)

    private var filterPredicate: (T) -> Boolean = { true }

    init {
        lastVisibleItem.onEach { item ->
            Timber.d("lastVisibleItem: $item")
            actionChannel.send(Action.CheckNeedLoad)
        }.launchIn(scope)

        scope.launch {
            for (action in actionChannel) {
                Timber.d("actionDeque: $action")

                when (action) {
                    is Action.LoadPage -> processLoadPage(action.key, action.direction)
                    is Action.RemovePages -> processRemovePages()
                    is Action.UpdateDataFlow -> processUpdateDataFlow()
                    is Action.Invalidate -> processInvalidate(full = action.full)
                    is Action.CheckNeedLoad -> processCheckNeedLoad()
                }
            }
        }
    }

    private suspend fun processCheckNeedLoad() {
        Timber.d("processCheckNeedLoad")
        val currentItem = lastVisibleItem.value
        val pages = pageHelper.getPages()
        if (currentItem == null && pages.isEmpty()) {
            actionChannel.send(Action.LoadPage(initialPage, direction = null))
            return
        }

        val currentList = pages.flatMap { it.data }
        val firstPage = pages.firstOrNull()
        val lastPage = pages.lastOrNull()

        val index = currentList.indexOfFirst { it == currentItem }
        if (index == -1 && lastPage?.nextKey != null) {
            actionChannel.send(Action.LoadPage(lastPage.nextKey, direction = Direction.End))
            return
        }


        Timber.d("processCheckNeedLoad index=$index, size=${currentList.size}")
        if (index >= currentList.size - threshold && lastPage != null) {
            lastPage.nextKey?.let { nextKey -> actionChannel.send(Action.LoadPage(nextKey, direction = Direction.End)) }
        }
        if (index <= threshold && firstPage != null && firstPage != lastPage) {
            firstPage.prevKey?.let { prevKey -> actionChannel.send(Action.LoadPage(prevKey, direction = Direction.Start)) }
        }
    }

    private suspend fun processInvalidate(full: Boolean) {
        Timber.d("processInvalidate")
        val currentPage = dataMutex.withLock {
            val lastVisibleItem = lastVisibleItem.value
            val page = findPageForItem(lastVisibleItem)
            loadingPages.values.forEach { it.cancel() }
            loadingPages.clear()
            pageHelper.clear()

            page
        }

        if (full || currentPage == null) {
            actionChannel.send(Action.UpdateDataFlow)
            actionChannel.send(Action.CheckNeedLoad)
            return
        }

        coroutineScope {
            val pagesToLoad = ArrayDeque<Int>()
            pagesToLoad.add(currentPage.key)

            val loadedKeys = mutableSetOf<Int>()

            while (true) {
                if (pagesToLoad.isEmpty()) break
                val pageKey = pagesToLoad.removeFirst()
                if (loadedKeys.contains(pageKey)) continue
                loadedKeys.add(pageKey)

                val page = loadPage(pageKey)

                page.prevKey?.let { pagesToLoad.addLast(it) }
                page.nextKey?.let { pagesToLoad.addLast(it) }

                val totalItems = dataMutex.withLock {
                    pageHelper.getPages().sumOf { it.data.size }
                }

                if (totalItems >= minItemsToLoad) break
            }
        }

        onNewPageLoaded()
    }

    private suspend fun processLoadPage(key: Int, direction: Direction?) {
        Timber.d("processLoadPage: key=$key")
        dataMutex.withLock {
            if (!needSkipLoading(key)) {
                loadPageAsync(key = key, direction = direction)
            }
        }
    }

    private suspend fun processRemovePages() {
        dataMutex.withLock {
            val pages = pageHelper.getPages().toList()
            val totalItems = pages.sumOf { it.data.size }
            if (totalItems <= maxItemsToKeep) return@withLock

            val currentItem = lastVisibleItem.value
            val currentList = pages.flatMap { it.data }

            val currentIndex = currentList.indexOfFirst { it == currentItem }
            val currentMappedItem = currentList.getOrNull(currentIndex)
            val currentPageKey = findPageForItem(currentMappedItem)?.key ?: return

            var itemsToRemove = totalItems - maxItemsToKeep

            val pagesSortedByDistance = pages.sortedByDescending { page -> abs(page.key - currentPageKey) }
            Timber.d("processRemovePages: pagesSortedByDistance=${pagesSortedByDistance.joinToString(",") { it.key.toString() }}")

            for (page in pagesSortedByDistance) {
                pageHelper.removePage(page)
                loadingPages.remove(page.key)?.cancel()
                itemsToRemove -= page.data.size
                Timber.d("processRemovePages: removed page key=${page.key}, distance=${abs(page.key - currentPageKey)}")

                if (itemsToRemove <= 0) break
            }
        }
    }

    private suspend fun processUpdateDataFlow() {
        Timber.d("processUpdateDataFlow")
        updateDataFlow()
    }

    private suspend fun updateDataFlow() {
        dataMutex.withLock {
            _data.value = pageHelper.getPages().flatMap { it.data }
        }
    }

    override fun invalidate() {
        actionChannel.trySend(Action.Invalidate(full = false))
    }

    override fun onItemVisible(item: T?) {
        lastVisibleItem.value = item
    }

    override fun destroy() {
        scope.launch {
            pageHelper.clear()
        }.invokeOnCompletion {
            scope.cancel()
            loadingPages.clear()
        }
    }

    private suspend fun findPageForItem(item: T?): Page<T, Int>? {
        val pages = pageHelper.getPages()
        val itemsToPage = pageHelper.getItemsByPage()
        return pages.firstOrNull { page ->
            itemsToPage[item] == page.key
        }
    }

    private suspend fun onNewPageLoaded() {
        Timber.d("onNewPageLoaded")
        coroutineContext.ensureActive()

        actionChannel.send(Action.RemovePages)
        actionChannel.send(Action.UpdateDataFlow)
        actionChannel.send(Action.CheckNeedLoad)
    }

    private suspend fun loadPage(page: Int): Page<T, Int> {
        Timber.d("loadPage: page=$page")

        val pageResult = primaryDataSource.loadData(page, pageSize)

        dataMutex.withLock {
            val predicate = filterPredicate
            val filteredPage = pageResult.copy(data = pageResult.data.filter(predicate))
            pageHelper.addPage(filteredPage)
        }

        return pageResult
    }

    private fun loadPageAsync(key: Int, direction: Direction?): Job {
        Timber.d("loadPageAsync: page=$key")

        _loadingState.update { current ->
            calculateLoadingState(currentState = current, direction = direction, isStartLoading = true)
        }
        loadingPages[key]?.cancel()
        val loadJob = scope.launch {
            ensureActive()
            loadPage(key)
            loadingPages.remove(key)
            _loadingState.update { current ->
                calculateLoadingState(currentState = current, direction = direction, isStartLoading = false)
            }
            onNewPageLoaded()
        }
        Timber.d("loadPageAsync: $key, finished")
        loadingPages[key] = loadJob
        return loadJob
    }

    private suspend fun needSkipLoading(key: Int): Boolean {
        Timber.d("needSkipLoading: $key")
        val alreadyLoaded = pageHelper.getPages().any { it.key == key }
        val alreadyLoading = loadingPages[key] != null
        val result = alreadyLoaded || alreadyLoading
        Timber.d("needSkipLoading: $key, alreadyLoaded=$alreadyLoaded, alreadyLoading=$alreadyLoading, result=$result")
        return result
    }

    private fun calculateLoadingState(currentState: LoadingState, direction: Direction?, isStartLoading: Boolean): LoadingState {
        return if (isStartLoading) {
            when (direction) {
                Direction.Start -> when (currentState) {
                    LoadingState.None -> LoadingState.Start
                    LoadingState.End -> LoadingState.Both
                    LoadingState.Start, LoadingState.Both -> currentState
                }

                Direction.End -> when (currentState) {
                    LoadingState.None -> LoadingState.End
                    LoadingState.Start -> LoadingState.Both
                    LoadingState.End, LoadingState.Both -> currentState
                }

                null -> currentState
            }
        } else {
            when (direction) {
                Direction.Start -> when (currentState) {
                    LoadingState.Start -> LoadingState.None
                    LoadingState.Both -> LoadingState.End
                    LoadingState.None, LoadingState.End -> currentState
                }

                Direction.End -> when (currentState) {
                    LoadingState.End -> LoadingState.None
                    LoadingState.Both -> LoadingState.Start
                    LoadingState.None, LoadingState.Start -> currentState
                }

                null -> currentState
            }
        }
    }

    fun updateFilterPredicate(predicate: (T) -> Boolean) {
        filterPredicate = predicate
        actionChannel.trySend(Action.Invalidate(full = true))
    }

    private enum class Direction {
        Start, End
    }

    private sealed class Action {
        data class LoadPage(val key: Int, val direction: Direction?) : Action()
        data object RemovePages : Action()
        data object UpdateDataFlow : Action()
        data class Invalidate(val full: Boolean) : Action()
        data object CheckNeedLoad : Action()
    }
}
