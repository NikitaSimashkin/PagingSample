package ru.kram.pagingsample.domain

import ru.kram.pagingsample.data.remote.model.CatDTO

data class CatsDomain(
    val cats: List<CatDTO>,
    val itemsBefore: Int?,
    val itemsAfter: Int?,
)