package ru.kram.pagerlib.model

data class Page<T, K>(
    val data: List<T>,
    val key: K,
    val nextKey: K?,
    val prevKey: K?,
    val itemsBefore: Int? = null,
    val itemsAfter: Int? = null,
)