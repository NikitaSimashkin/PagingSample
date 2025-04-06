package ru.kram.pagerlib.data

import ru.kram.pagerlib.model.Page

interface DataSource<T, K> {
    suspend fun loadData(key: K, pageSize: Int): T
}

interface PagedDataSource<T, K> : DataSource<Page<T, K>, K> {
    override suspend fun loadData(key: K, pageSize: Int): Page<T, K>
}

