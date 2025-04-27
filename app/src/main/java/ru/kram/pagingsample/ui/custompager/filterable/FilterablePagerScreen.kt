package ru.kram.pagingsample.ui.custompager.filterable

import PrimaryActionButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.onStart
import org.koin.androidx.compose.koinViewModel
import ru.kram.pagerlib.model.LoadingState
import ru.kram.pagingsample.R
import ru.kram.pagingsample.ui.filmlist.FilmItem
import ru.kram.pagingsample.ui.filmlist.FilmListBaseScreen
import ru.kram.pagingsample.ui.filmlist.model.FilmItemData
import timber.log.Timber

@Composable
fun FilterablePagerScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<FilterablePagerViewModel>()
    val state = viewModel.filmPagingState.collectAsStateWithLifecycle()

    val scrollState = rememberLazyListState()

    FilmListBaseScreen(
        infoBlockData = state.value.infoBlockData,
        onAddOneFilms = viewModel::onAddOneFilms,
        onAdd100Films = viewModel::onAdd100Films,
        onClearAllUserFilms = viewModel::clearUserFilms,
        onClearLocalDb = viewModel::clearLocalDb,
        modifier = modifier,
        additionalSettings = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                var value by remember { mutableStateOf(state.value.filterYear.toString()) }
                Text(
                    text = stringResource(id = R.string.films_after),
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    keyboardOptions = remember {
                        KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
                PrimaryActionButton(
                    text = stringResource(id = R.string.apply_filter),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.applyYearFilter(value.toIntOrNull()) }
                )
            }
        }
    ) {
        Box(
            Modifier.fillMaxSize()
        ) {
            if (state.value.loadingState is LoadingState.Start) {
                LoadingIndicator()
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = scrollState,
            ) {
                items(
                    count = state.value.films.size,
                    key = { index -> state.value.films[index]!!.id },
                ) { index ->
                    val film = state.value.films[index] ?: return@items
                    FilmItem(
                        filmItemData = film,
                        onDeleteClick = { viewModel.deleteFilm(film) },
                        onRenameClick = {},
                        index = null,
                    )
                }

                if (state.value.loadingState is LoadingState.End || state.value.loadingState is LoadingState.Both) {
                    item(key = "loading end") {
                        LoadingIndicator()
                    }
                }
            }

            PagerObserver(
                lazyListState = scrollState,
                onFilmVisible = viewModel::onItemVisible,
                filmsState = state,
            )
        }
    }
}

@Composable
private fun PagerObserver(
    lazyListState: LazyListState,
    filmsState: State<FilterableFilmsState>,
    onFilmVisible: (FilmItemData?) -> Unit,
) {
    LaunchedEffect(lazyListState, filmsState) {
        snapshotFlow {
            val filmSize = filmsState.value.films.size
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val first = visibleItems.first().index
                val last = visibleItems.last().index
                (first + last) / 2 to filmSize
            } else {
                lazyListState.firstVisibleItemIndex to filmsState
            }
        }.onStart {
            emit(0 to 0)
        }.collect { (index, _) ->
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