package ru.kram.pagingsample.ui.custompager.simplepagerloading

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.kram.pagerlib.model.LoadingState
import ru.kram.pagingsample.ui.catlist.model.CatItemData
import ru.kram.pagingsample.ui.catlist.model.CatsScreenState

data class CatsWithLoadingState(
    val cats: List<CatItemData>,
    val loadingState: LoadingState,
)