package ru.kram.pagerlib.model

data class PageWithIndex<K>(
    val page: K,
    val indexInPage: Int,
)