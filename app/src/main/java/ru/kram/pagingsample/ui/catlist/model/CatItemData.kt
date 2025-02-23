package ru.kram.pagingsample.ui.catlist.model

data class CatItemData(
    val id: String,
    val imageUrl: String,
    val name: String,
    val breed: String,
    val age: Int,
    val createdAt: Long,
)