package ru.kram.pagingsample.domain

data class FilmDomain(
    val id: String,
    val imageUrl: String,
    val name: String,
    val createdAt: Long,
    val year: Int,
    val number: Int,
)