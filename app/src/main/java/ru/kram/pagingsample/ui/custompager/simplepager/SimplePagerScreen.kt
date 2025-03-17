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
import ru.kram.pagingsample.ui.catlist.CatItem
import ru.kram.pagingsample.ui.catlist.CatListBaseScreen

@Composable
fun SimplePagerScreen(
    modifier: Modifier = Modifier,
    smallCats: Boolean = false,
) {
    val viewModel = koinViewModel<SimplePagerViewModel>()
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
        val cats = viewModel.cats.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            snapshotFlow {
                viewModel.updateListInfo(cats.value.size, cats.value)
            }.collect {}
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = scrollState,
        ) {
            items(
                count = cats.value.size,
                key = { index -> cats.value[index].id },
            ) { index ->
                val cat = cats.value[index]
                CatItem(
                    catItemData = cat,
                    onDeleteClick = { viewModel.deleteCat(cat) },
                    onRenameClick = {},
                    showOnlyNumber = smallCats,
                    modifier = if (smallCats) Modifier.height(75.dp) else Modifier
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
        }.collect { visibleCat ->
            onIndexVisible(visibleCat)
        }
    }
}