package ru.kram.niksi

import kotlinx.coroutines.flow.StateFlow
import ru.kram.niksi.model.LoadingState

interface Pager<T, K> {
    val data: StateFlow<List<T>>

    fun invalidate(resetTerminalPages: Boolean = false)
    fun onItemVisible(item: K?)
}

interface HasLoadingState {
    val loadingState: StateFlow<LoadingState>
}

interface Filterable<T> {
    fun setFilter(filter: (T) -> Boolean)
}

interface Jumpable<K> {
    fun jumpTo(key: K)
}