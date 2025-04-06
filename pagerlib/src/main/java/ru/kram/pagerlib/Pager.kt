package ru.kram.pagerlib

import kotlinx.coroutines.flow.StateFlow
import ru.kram.pagerlib.model.LoadingState

interface Pager<T, K> {
    val data: StateFlow<List<T?>>

    fun invalidate()

    fun onItemVisible(item: K?)

    fun destroy()
}

interface HasLoadingState {
    val loadingState: StateFlow<LoadingState>
}