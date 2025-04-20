package ru.kram.pagingsample.ui.filmlist.model

data class FilmsScreenState(
    val infoBlockData: InfoBlockData,
    val pagesBlockData: PagesBlockData,
) {
    companion object {
        val EMPTY = FilmsScreenState(
            infoBlockData = InfoBlockData.EMPTY,
            pagesBlockData = PagesBlockData.EMPTY,
        )
    }
}