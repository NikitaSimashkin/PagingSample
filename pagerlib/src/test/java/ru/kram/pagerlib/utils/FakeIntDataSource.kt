package ru.kram.pagerlib.utils

import ru.kram.pagerlib.model.Page

class FakeIntDataSource : ru.kram.pagerlib.data.PagedDataSource<Int, Int> {
    override suspend fun loadData(key: Int, pageSize: Int): Page<Int, Int> {
        val start = (key - 1) * pageSize + 1
        val data = (start until start + pageSize).toList()
        val prevKey = if (key > 1) key - 1 else null
        val nextKey = key + 1 
        return Page(
            key = key, 
            data = data, 
            prevKey = prevKey, 
            nextKey = nextKey
        )
    }
}