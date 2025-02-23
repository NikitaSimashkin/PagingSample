package ru.kram.pagingsample.ui.catlist.model

data class CatsScreenState(
    val infoBlockData: InfoBlockData,
    val pagesBlockData: PagesBlockData,
) {
    companion object {
        val EMPTY = CatsScreenState(
            infoBlockData = InfoBlockData.EMPTY,
            pagesBlockData = PagesBlockData.EMPTY,
        )
    }
}