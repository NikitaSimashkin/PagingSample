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
import ru.kram.pagingsample.ui.catlist.CatItem
import ru.kram.pagingsample.ui.catlist.CatItemPlaceholder
import ru.kram.pagingsample.ui.catlist.CatListBaseScreen
import timber.log.Timber

@Composable
fun JumpablePagerScreen(
    modifier: Modifier = Modifier,
    smallCats: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val viewModel = koinViewModel<JumpablePagerViewModel>()
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    val scrollState = rememberLazyListState()

    CatListBaseScreen(
        infoBlockData = state.infoBlockData,
        pagesBlockData = state.pagesBlockData,
        onAddOneCats = viewModel::onAddOneCats,
        onAdd100Cats = viewModel::onAdd100Cats,
        onClearAllUserCats = viewModel::clearUserCats,
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
            val catState = viewModel.cats.collectAsStateWithLifecycle()

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = scrollState,
            ) {
                items(
                    count = catState.value.size,
                    key = { index -> catState.value[index]?.id ?: index },
                ) { index ->
                    val cat = catState.value[index]
                    if (cat == null) {
                        CatItemPlaceholder(
                            number = index,
                            showOnlyNumber = smallCats,
                        )
                    } else {
                        CatItem(
                            catItemData = cat,
                            onDeleteClick = { viewModel.deleteCat(cat) },
                            onRenameClick = {},
                            showOnlyNumber = smallCats,
                            modifier = if (smallCats) Modifier.height(75.dp) else Modifier
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
            Timber.d("onCatVisible: index=$index")
            onIndexVisible(index)
        }
    }
}