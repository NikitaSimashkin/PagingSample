package ru.kram.pagingsample.ui.catlist.both

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.kram.pagingsample.ui.custompager.CustomPagerScreen
import ru.kram.pagingsample.ui.paging3.Paging3Screen

@Composable
fun BothScreen(
    modifier: Modifier = Modifier
) {

    Row {
        CustomPagerScreen(
            modifier = modifier.weight(1f),
            smallCats = true,
        )
        Paging3Screen(
            modifier = modifier.weight(1f),
            smallCats = true,
        )
    }
}