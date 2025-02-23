package ru.kram.pagingsample.ui.paging3

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import org.koin.androidx.compose.koinViewModel
import ru.kram.pagingsample.ui.catlist.CatItem
import ru.kram.pagingsample.ui.catlist.CatListBaseScreen
import ru.kram.pagingsample.ui.navigation.Paging3Component
import timber.log.Timber

@Composable
fun Paging3Screen(
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<Paging3ViewModel>()

    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()
    CatListBaseScreen(
        infoBlockData = state.infoBlockData,
        pagesBlockData = state.pagesBlockData,
        onAddOneCats = viewModel::onAddOneCats,
        onAdd100Cats = viewModel::onAdd100Cats,
        onClearLocalDb = viewModel::clearLocalDb,
        onClearAllUserCats = viewModel::clearUserCats,
        modifier = modifier,
    ) {
        val cats = viewModel.catList.collectAsLazyPagingItems()
        LaunchedEffect(cats.itemSnapshotList) {
            viewModel.updateListInfo(cats.itemCount, cats.itemSnapshotList)
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = scrollState,
        ) {
            items(
                count = cats.itemCount,
                key = cats.itemKey { it.id },
            ) { index ->
                val cat = cats[index]
                if (cat != null) {
                    CatItem(
                        catItemData = cat,
                        onDeleteClick = viewModel::deleteCat,
                        onRenameClick = {},
                    )
                }
            }
        }
    }
}
