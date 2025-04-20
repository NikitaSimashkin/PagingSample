package ru.kram.pagingsample.ui.filmlist.both

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.kram.pagingsample.ui.custompager.simplepager.SimplePagerScreen
import ru.kram.pagingsample.ui.paging3.Paging3Screen

@Composable
fun BothScreen(
    modifier: Modifier = Modifier
) {

    Row {
        SimplePagerScreen(
            modifier = modifier.weight(1f),
            smallFilms = true,
        )
        Paging3Screen(
            modifier = modifier.weight(1f),
            smallFilms = true,
        )
    }
}