package ru.kram.pagingsample.ui.filmlist.model

data class FilmItemData(
    val id: String,
    val imageUrl: String,
    val name: String,
    val year: Int,
    val createdAt: Long,
    val number: Int,
)