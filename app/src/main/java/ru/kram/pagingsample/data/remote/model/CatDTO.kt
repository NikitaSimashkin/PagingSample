package ru.kram.pagingsample.data.remote.model

data class CatDTO(
    val id: String,
    val imageUrl: String,
    val name: String,
    val breed: String,
    val createdAt: Long,
    val age: Int,
)