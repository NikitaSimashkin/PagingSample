package ru.kram.pagingsample.ui.custompager.simplepagerloading

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.onStart
import org.koin.androidx.compose.koinViewModel
import ru.kram.niksi.model.LoadingState
import ru.kram.pagingsample.ui.catlist.CatItem
import ru.kram.pagingsample.ui.catlist.CatListBaseScreen
import ru.kram.pagingsample.ui.catlist.model.CatItemData
import timber.log.Timber

@Composable
fun SimplePagerWithLoadingStateScreen(
    modifier: Modifier = Modifier,
    smallCats: Boolean = false,
) {
    val viewModel = koinViewModel<SimplePagerWithLoadingStateViewModel>()
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
        Column(
            Modifier.fillMaxSize()
        ) {
            val cats = viewModel.cats.collectAsStateWithLifecycle()
            val loading = viewModel.loadingState.collectAsStateWithLifecycle()
            val offset = remember { mutableIntStateOf(0) }

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
                if (loading.value is LoadingState.Start || loading.value is LoadingState.Both) {
                    item(
                        key = "loading start"
                    ) {
                        offset.intValue = 1
                        LoadingIndicator()
                    }
                }

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

                if (loading.value is LoadingState.End || loading.value is LoadingState.Both) {
                    item(
                        key = "loading end"
                    ) {
                        LoadingIndicator()
                    }
                }
            }

            PagerObserver(
                lazyListState = scrollState,
                onCatVisible = viewModel::onCatVisible,
                catsState = cats,
                offset = offset,
            )
        }
    }
}

@Composable
private fun PagerObserver(
    lazyListState: LazyListState,
    catsState: State<List<CatItemData>>,
    onCatVisible: (CatItemData?) -> Unit,
    offset: State<Int>,
) {
    LaunchedEffect(lazyListState, catsState, offset) {
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
            val cats = catsState.value
            Timber.d("onCatVisible: index=$index, cat=${cats.getOrNull(index)?.name}")
            onCatVisible(cats.getOrNull(index))
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