package ru.kram.pagingsample.ui.catlist

import PagesBottomBlock
import PrimaryActionButton
import TopInfoBlock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kram.pagingsample.R
import ru.kram.pagingsample.designsystem.bottomsheet.BaseBottomSheet
import ru.kram.pagingsample.designsystem.theme.Colors
import ru.kram.pagingsample.designsystem.theme.PagingSampleTheme
import ru.kram.pagingsample.ui.catlist.model.InfoBlockData
import ru.kram.pagingsample.ui.catlist.model.PagesBlockData

@Composable
fun CatListBaseScreen(
    infoBlockData: InfoBlockData,
    pagesBlockData: PagesBlockData,
    modifier: Modifier = Modifier,
    onAddOneCats: () -> Unit = {},
    onAdd100Cats: () -> Unit = {},
    onClearLocalDb: () -> Unit = {},
    onClearAllUserCats: () -> Unit = {},
    onPageClick: (Int) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val isAddCatsExpanded = remember { mutableStateOf(false) }
    val isSettingsExpanded = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(Colors.background)
            .safeContentPadding()
            .fillMaxSize()
    ) {
        TopInfoBlock(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxHeight(0.1f)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            onAddClick = { isAddCatsExpanded.value = true },
            infoBlockData = infoBlockData,
            onSettingsClick = { isSettingsExpanded.value = true }
        )

        Box(
            modifier = Modifier.weight(1f)
        ) {
            content()
        }

        PagesBottomBlock(
            pagesBlockData = pagesBlockData,
            onPageClick = onPageClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )
    }

    AddCatsBottomSheet(
        isExpanded = isAddCatsExpanded,
        onAddOneCat = onAddOneCats,
        onAdd100Cats = onAdd100Cats,
    )
    SettingsBottomSheet(
        isExpanded = isSettingsExpanded,
        onClearLocalDb = onClearLocalDb,
        onClearAllUserCats = onClearAllUserCats,
    )
}

@Composable
private fun SettingsBottomSheet(
    isExpanded: MutableState<Boolean>,
    onClearLocalDb: () -> Unit,
    onClearAllUserCats: () -> Unit,
) {
    if (!isExpanded.value) return

    BaseBottomSheet(
        onDismissRequest = {
            isExpanded.value = false
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrimaryActionButton(
                text = stringResource(R.string.clear_local_db),
                onClick = onClearLocalDb
            )
            PrimaryActionButton(
                text = stringResource(R.string.clear_all_user_cats),
                onClick = onClearAllUserCats
            )
        }
    }
}

@Composable
private fun AddCatsBottomSheet(
    isExpanded: MutableState<Boolean>,
    onAddOneCat: () -> Unit,
    onAdd100Cats: () -> Unit,
) {
    if (!isExpanded.value) return

    BaseBottomSheet(
        onDismissRequest = {
            isExpanded.value = false
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrimaryActionButton(
                text = stringResource(R.string.add_one_cat),
                onClick = onAddOneCat
            )
            PrimaryActionButton(
                text = stringResource(R.string.add_100_cats),
                onClick = onAdd100Cats
            )
        }
    }
}

@Preview
@Composable
private fun CatListScreenPreview() {
    PagingSampleTheme {
        CatListBaseScreen(
            infoBlockData = InfoBlockData(
                text1Left = "text1Left",
                text1Right = "text1Right",
                text2Left = "text2Left",
                text2Right = "text2Right",
                text3Left = "text3Left",
                text3Right = "text3Right",
            ),
            pagesBlockData = PagesBlockData(
                firstPage = 1,
                lastPage = 10,
                currentPage = 1
            ),
            content = {},
            onAdd100Cats = {},
            onAddOneCats = {},
            onClearLocalDb = {},
        )
    }
}