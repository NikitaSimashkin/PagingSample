package ru.kram.pagingsample.ui.filmlist.custompagermenu

import PrimaryActionButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.kram.pagingsample.R
import ru.kram.pagingsample.designsystem.theme.Colors
import ru.kram.pagingsample.ui.navigation.menu.CustomPagerScreenType
import ru.kram.pagingsample.ui.navigation.menu.CustomPagersMenuComponent

@Composable
fun CustomPagersMenuScreen(
    component: CustomPagersMenuComponent,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Colors.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            PrimaryActionButton(
                text = stringResource(R.string.simple_pager_title),
                modifier = Modifier.fillMaxWidth(),
                onClick = { component.onPagerScreenSelected(CustomPagerScreenType.SIMPLE_PAGER) }
            )

            PrimaryActionButton(
                text = stringResource(R.string.simple_pager_with_loading_state_title),
                modifier = Modifier.fillMaxWidth(),
                onClick = { component.onPagerScreenSelected(CustomPagerScreenType.SIMPLE_PAGER_WITH_LOADING_STATE) }
            )

            PrimaryActionButton(
                text = stringResource(R.string.jumpable_pager_title),
                modifier = Modifier.fillMaxWidth(),
                onClick = { component.onPagerScreenSelected(CustomPagerScreenType.JUMPABLE_PAGER) }
            )
        }
    }
}
