package ru.kram.pagingsample.domain

data class FilmsDomain(
    val films: List<FilmDomain>,
    val totalCount: Int,
)