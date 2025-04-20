package ru.kram.pagingsample.ui.custompager.simplepager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.onStart
import org.koin.androidx.compose.koinViewModel
import ru.kram.pagingsample.ui.filmlist.FilmItem
import ru.kram.pagingsample.ui.filmlist.FilmListBaseScreen
import ru.kram.pagingsample.ui.filmlist.FilmItem

@Composable
fun SimplePagerScreen(
    modifier: Modifier = Modifier,
    smallFilms: Boolean = false,
) {
    val viewModel = koinViewModel<SimplePagerViewModel>()
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
        val films = viewModel.films.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            snapshotFlow {
                viewModel.updateListInfo(films.value.size, films.value)
            }.collect {}
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = scrollState,
        ) {
            items(
                count = films.value.size,
                key = { index -> films.value[index].id },
            ) { index ->
                val film = films.value[index]
                FilmItem(
                    filmItemData = film,
                    onDeleteClick = { viewModel.deleteFilm(film) },
                    onRenameClick = {},
                    showOnlyNumber = smallFilms,
                    modifier = if (smallFilms) Modifier.height(75.dp) else Modifier,
                    index = index,
                )
            }
        }

        PagerObserver(
            lazyListState = scrollState,
            onIndexVisible = viewModel::onItemVisible,
        )
    }
}

@Composable
fun PagerObserver(
    lazyListState: LazyListState,
    onIndexVisible: (Int) -> Unit,
) {
    LaunchedEffect(lazyListState) {
        snapshotFlow {
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val first = visibleItems.first().index
                val last = visibleItems.last().index
                (first + last) / 2
            } else {
                lazyListState.firstVisibleItemIndex
            }
        }.onStart {
            emit(0)
        }.collect { visibleFilm ->
            onIndexVisible(visibleFilm)
        }
    }
}