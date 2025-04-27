package ru.kram.pagingsample.ui.filmlist

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
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
import ru.kram.pagingsample.ui.filmlist.model.InfoBlockData
import ru.kram.pagingsample.ui.filmlist.model.PagesBlockData

@Composable
fun FilmListBaseScreen(
    infoBlockData: InfoBlockData,
    modifier: Modifier = Modifier,
    onAddOneFilms: () -> Unit = {},
    onAdd100Films: () -> Unit = {},
    onClearLocalDb: () -> Unit = {},
    onClearAllUserFilms: () -> Unit = {},
    additionalSettings: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val isAddFilmsExpanded = remember { mutableStateOf(false) }
    val isSettingsExpanded = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(Colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        TopInfoBlock(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxHeight(0.1f)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            onAddClick = { isAddFilmsExpanded.value = true },
            infoBlockData = infoBlockData,
            onSettingsClick = { isSettingsExpanded.value = true }
        )

        Box(
            modifier = Modifier.weight(1f)
                .padding(top = 16.dp)
        ) {
            content()
        }
    }

    AddFilmsBottomSheet(
        isExpanded = isAddFilmsExpanded,
        onAddOneFilm = onAddOneFilms,
        onAdd100Films = onAdd100Films,
    )
    SettingsBottomSheet(
        isExpanded = isSettingsExpanded,
        onClearLocalDb = onClearLocalDb,
        onClearAllUserFilms = onClearAllUserFilms,
        additionalSettings = additionalSettings,
    )
}

@Composable
private fun SettingsBottomSheet(
    additionalSettings: @Composable () -> Unit,
    isExpanded: MutableState<Boolean>,
    onClearLocalDb: () -> Unit,
    onClearAllUserFilms: () -> Unit,
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
                text = stringResource(R.string.clear_all_user_films),
                onClick = onClearAllUserFilms
            )
            HorizontalDivider()

            additionalSettings()
        }
    }
}

@Composable
private fun AddFilmsBottomSheet(
    isExpanded: MutableState<Boolean>,
    onAddOneFilm: () -> Unit,
    onAdd100Films: () -> Unit,
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
                text = stringResource(R.string.add_one_film),
                onClick = onAddOneFilm
            )
            PrimaryActionButton(
                text = stringResource(R.string.add_100_films),
                onClick = onAdd100Films
            )
        }
    }
}

@Preview
@Composable
private fun FilmListScreenPreview() {
    PagingSampleTheme {
        FilmListBaseScreen(
            infoBlockData = InfoBlockData(
                text1Left = "text1Left",
                text1Right = "text1Right",
                text2Left = "text2Left",
                text2Right = "text2Right",
                text3Left = "text3Left",
                text3Right = "text3Right",
            ),
            content = {},
            onAdd100Films = {},
            onAddOneFilms = {},
            onClearLocalDb = {},
        )
    }
}