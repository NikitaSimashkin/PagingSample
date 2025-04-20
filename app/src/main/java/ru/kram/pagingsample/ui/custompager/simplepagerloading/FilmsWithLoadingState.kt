package ru.kram.pagingsample.ui.custompager.simplepagerloading

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.kram.pagerlib.model.LoadingState
import ru.kram.pagingsample.ui.filmlist.model.FilmItemData
import ru.kram.pagingsample.ui.filmlist.model.FilmsScreenState

data class FilmsWithLoadingState(
    val films: List<FilmItemData>,
    val loadingState: LoadingState,
)