package ru.kram.pagingsample.ui.custompager.jumpable

import ru.kram.pagingsample.ui.filmlist.model.FilmItemData
import ru.kram.pagingsample.ui.filmlist.model.InfoBlockData
import ru.kram.pagingsample.ui.filmlist.model.PagesBlockData

data class JumpableScreenState(
    val infoBlockData: InfoBlockData,
    val pagesBlockData: PagesBlockData,
    val page: Int?,
    val films: List<FilmItemData?>,
) {

    companion object {
        val EMPTY = JumpableScreenState(
            infoBlockData = InfoBlockData.EMPTY,
            pagesBlockData = PagesBlockData.EMPTY,
            page = null,
            films = emptyList(),
        )
    }
}