package ru.kram.pagingsample.ui.custompager.jumpablepager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.kram.pagingsample.ui.filmlist.FilmItem
import ru.kram.pagingsample.ui.filmlist.FilmItemPlaceholder
import ru.kram.pagingsample.ui.filmlist.FilmListBaseScreen
import ru.kram.pagingsample.ui.filmlist.FilmItem
import ru.kram.pagingsample.ui.filmlist.FilmItemPlaceholder
import timber.log.Timber

@Composable
fun JumpablePagerScreen(
    modifier: Modifier = Modifier,
    smallFilms: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val viewModel = koinViewModel<JumpablePagerViewModel>()
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    val scrollState = rememberLazyListState()

    FilmListBaseScreen(
        infoBlockData = state.infoBlockData,
        pagesBlockData = state.pagesBlockData,
        onAddOneFilms = viewModel::onAddOneFilms,
        onAdd100Films = viewModel::onAdd100Films,
        onClearAllUserFilms = viewModel::clearUserFilms,
        onClearLocalDb = viewModel::clearLocalDb,
        onPageClick = { page ->
            scope.launch {
                scrollState.scrollToItem(index = page * JumpablePagerViewModel.PAGE_SIZE)
            }
        },
        modifier = modifier,
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            val filmState = viewModel.films.collectAsStateWithLifecycle()

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = scrollState,
            ) {
                items(
                    count = filmState.value.size,
                    key = { index -> filmState.value[index]?.id ?: index },
                ) { index ->
                    val film = filmState.value[index]
                    if (film == null) {
                        FilmItemPlaceholder(
                            index = index,
                            showOnlyNumber = smallFilms,
                        )
                    } else {
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
            }

            PagerObserver(
                lazyListState = scrollState,
                onIndexVisible = viewModel::onIndexVisible,
            )
        }
    }
}

@Composable
private fun PagerObserver(
    lazyListState: LazyListState,
    onIndexVisible: (Int?) -> Unit,
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
        }.collect { index ->
            Timber.d("onFilmVisible: index=$index")
            onIndexVisible(index)
        }
    }
}