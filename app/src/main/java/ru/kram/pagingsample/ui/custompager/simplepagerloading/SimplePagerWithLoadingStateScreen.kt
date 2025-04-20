package ru.kram.pagingsample.ui.custompager.simplepagerloading

import PrimaryActionButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.onStart
import org.koin.androidx.compose.koinViewModel
import ru.kram.pagerlib.model.LoadingState
import ru.kram.pagingsample.ui.filmlist.FilmItem
import ru.kram.pagingsample.ui.filmlist.FilmListBaseScreen
import ru.kram.pagingsample.ui.filmlist.FilmItem
import ru.kram.pagingsample.ui.filmlist.model.FilmItemData
import timber.log.Timber

@Composable
fun SimplePagerWithLoadingStateScreen(
    modifier: Modifier = Modifier,
    smallFilms: Boolean = false,
) {
    val viewModel = koinViewModel<SimplePagerWithLoadingStateViewModel>()
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    val scrollState = rememberLazyListState()

    FilmListBaseScreen(
        infoBlockData = state.infoBlockData,
        pagesBlockData = state.pagesBlockData,
        onAddOneFilms = viewModel::onAddOneFilms,
        onAdd100Films = viewModel::onAdd100Films,
        onClearAllUserFilms = viewModel::clearUserFilms,
        onClearLocalDb = viewModel::clearLocalDb,
        modifier = modifier,
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            val filmState = viewModel.filmPagingState.collectAsStateWithLifecycle()
            val offset = remember { mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                snapshotFlow {
                    Timber.d("loadingState=${filmState.value.loadingState}")
                    viewModel.updateListInfo(filmState.value.films.size, filmState.value.films)
                }.collect {}
            }

            if (filmState.value.loadingState is LoadingState.Start) {
                LoadingIndicator()
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = scrollState,
            ) {
                items(
                    count = filmState.value.films.size,
                    key = { index -> filmState.value.films[index].id },
                ) { index ->
                    val film = filmState.value.films[index]
                    FilmItem(
                        filmItemData = film,
                        onDeleteClick = { viewModel.deleteFilm(film) },
                        onRenameClick = {},
                        showOnlyNumber = smallFilms,
                        modifier = if (smallFilms) Modifier.height(75.dp) else Modifier,
                        index = index,
                    )
                }

                if (filmState.value.loadingState is LoadingState.End || filmState.value.loadingState is LoadingState.Both) {
                    item(key = "loading end") {
                        LoadingIndicator()
                    }
                }
            }

            PagerObserver(
                lazyListState = scrollState,
                onFilmVisible = viewModel::onFilmVisible,
                filmsState = filmState,
                offset = offset,
            )
        }
    }
}

@Composable
private fun PagerObserver(
    lazyListState: LazyListState,
    filmsState: State<FilmsWithLoadingState>,
    onFilmVisible: (FilmItemData?) -> Unit,
    offset: State<Int>,
) {
    LaunchedEffect(lazyListState, filmsState, offset) {
        snapshotFlow {
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val first = visibleItems.first().index
                val last = visibleItems.last().index
                (first + last) / 2
            } else {
                lazyListState.firstVisibleItemIndex
            } - offset.value
        }.onStart {
            emit(0)
        }.collect { index ->
            val state = filmsState.value
            Timber.d("onFilmVisible: index=$index, film=${state.films.getOrNull(index)?.name}")
            onFilmVisible(state.films.getOrNull(index))
        }
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = modifier.size(50.dp)
        )
    }
}