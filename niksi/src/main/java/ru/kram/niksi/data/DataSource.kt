package ru.kram.niksi.data

import ru.kram.niksi.model.Page

interface DataSource<T, K> {
    suspend fun loadData(key: K, pageSize: Int): T
}

interface PagedDataSource<T, K> : DataSource<Page<T, K>, K> {
    override suspend fun loadData(key: K, pageSize: Int): Page<T, K>
}

