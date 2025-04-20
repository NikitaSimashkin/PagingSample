package ru.kram.pagingsample.data.remote.model

data class FilmDTO(
    val id: String,
    val imageUrl: String,
    val name: String,
    val createdAt: Long,
    val age: Int,
    val number: Int,
)