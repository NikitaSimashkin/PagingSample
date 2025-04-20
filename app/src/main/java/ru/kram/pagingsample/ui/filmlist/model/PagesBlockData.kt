package ru.kram.pagingsample.ui.filmlist.model

data class PagesBlockData(
    val firstPage: Int,
    val lastPage: Int,
    val currentPage: Int,
) {
    companion object {
        val EMPTY = PagesBlockData(
            firstPage = 0,
            lastPage = -1,
            currentPage = 0,
        )
    }
}