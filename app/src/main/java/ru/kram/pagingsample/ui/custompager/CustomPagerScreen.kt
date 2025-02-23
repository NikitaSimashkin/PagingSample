package ru.kram.pagingsample.ui.custompager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import ru.kram.pagingsample.ui.catlist.CatItem
import ru.kram.pagingsample.ui.catlist.CatListBaseScreen

@Composable
fun CustomPagerScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<CustomPagerViewModel>()
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    val scrollState = rememberLazyListState()

    CatListBaseScreen(
        infoBlockData = state.infoBlockData,
        pagesBlockData = state.pagesBlockData,
        onAddOneCats = viewModel::onAddOneCats,
        onAdd100Cats = viewModel::onAdd100Cats,
        onClearAllUserCats = viewModel::clearUserCats,
        onClearLocalDb = viewModel::clearLocalDb,
        modifier = modifier,
    ) {
        val cats by viewModel.cats.collectAsStateWithLifecycle()
        LaunchedEffect(cats) {
            viewModel.updateListInfo(cats.size, cats)
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = scrollState,
        ) {
            items(
                count = cats.size,
                key = { index -> cats[index]?.id ?: index },
            ) { index ->
                val cat = cats[index]
                if (cat != null) {
                    CatItem(
                        catItemData = cat,
                        onDeleteClick = { viewModel.deleteCat(cat) },
                        onRenameClick = {},
                    )
                }
            }
        }
    }

    PagerObserver(
        lazyListState = scrollState,
        onIndexChanged = viewModel::onIndexChanged,
    )
}

@Composable
fun PagerObserver(
    lazyListState: LazyListState,
    onIndexChanged: (Int) -> Unit,
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
        }.collect { visibleIndex ->
            onIndexChanged(visibleIndex)
        }
    }
}