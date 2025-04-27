package ru.kram.pagerlib.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.kram.pagerlib.model.Page
import timber.log.Timber

class PageHelper<T, K: Comparable<K>> {

    private val pages = sortedMapOf<K, Page<T, K>>()
    private val itemsToPageKey = mutableMapOf<T, K>()

    private val mutex = Mutex()

    suspend fun getPages(): List<Page<T, K>> {
        return mutex.withLock {
            pages.values.toList()
        }
    }

    suspend fun getItemsByPage(): Map<T, K> {
        return mutex.withLock {
            itemsToPageKey.toMap()
        }
    }

    suspend fun addPage(page: Page<T, K>) {
        mutex.withLock {
            pages[page.key] = page
            page.data.forEach { item ->
                itemsToPageKey[item] = page.key
            }
        }
    }

    suspend fun removePage(page: Page<T, K>) {
        mutex.withLock {
            pages.remove(page.key)
            page.data.forEach { item ->
                itemsToPageKey.remove(item)
            }
        }
    }

    suspend fun clear() {
        Timber.d("clear pages")
        mutex.withLock {
            pages.clear()
            itemsToPageKey.clear()
            Timber.d("clear pages keys=${pages.keys.joinToString(",")}")
        }
    }
}