package ru.kram.pagingsample.ui.paging3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import ru.kram.pagingsample.ui.filmlist.FilmListBaseScreen
import ru.kram.pagingsample.ui.filmlist.FilmItem
import ru.kram.pagingsample.ui.filmlist.model.FilmItemData

@Composable
fun Paging3Screen(
    modifier: Modifier = Modifier,
    smallFilms: Boolean = false,
) {
    val viewModel = koinViewModel<Paging3ViewModel>()

    val infoBlockData by viewModel.screenState.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()
    FilmListBaseScreen(
        infoBlockData = infoBlockData,
        onAddOneFilms = viewModel::onAddOneFilms,
        onAdd100Films = viewModel::onAdd100Films,
        onClearLocalDb = viewModel::clearLocalDb,
        onClearAllUserFilms = viewModel::clearUserFilms,
        modifier = modifier,
    ) {
        val filmsPagingData: Flow<PagingData<FilmItemData>> =
            viewModel.filmsList

        val films: LazyPagingItems<FilmItemData> =
            filmsPagingData.collectAsLazyPagingItems()

        if (films.loadState.append is LoadState.Loading) {
            CircularProgressIndicator()
        }


        LaunchedEffect(films.itemSnapshotList) {
            viewModel.updateListInfo(films.itemCount, films.itemSnapshotList)
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = scrollState,
        ) {
            items(
                count = films.itemCount,
                key = films.itemKey { it.id },
            ) { index ->
                val film = films[index]
                if (film != null) {
                    FilmItem(
                        filmItemData = film,
                        onDeleteClick = viewModel::deleteFilm,
                        onRenameClick = {},
                        showOnlyNumber = smallFilms,
                        modifier = if (smallFilms) Modifier.height(75.dp) else Modifier,
                        index = index,
                    )
                }
            }
        }
    }
}
