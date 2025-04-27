package ru.kram.pagingsample.ui.custompager.filterable

import ru.kram.pagerlib.model.LoadingState
import ru.kram.pagingsample.ui.filmlist.model.FilmItemData
import ru.kram.pagingsample.ui.filmlist.model.InfoBlockData

data class FilterableFilmsState(
    val films: List<FilmItemData?>,
    val loadingState: LoadingState,
    val filterYear: Int,
    val infoBlockData: InfoBlockData,
) {

    companion object {
        val EMPTY = FilterableFilmsState(
            films = emptyList(),
            loadingState = LoadingState.None,
            filterYear = 0,
            infoBlockData = InfoBlockData.EMPTY,
        )
    }
}