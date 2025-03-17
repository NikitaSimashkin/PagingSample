package ru.kram.pagingsample.ui.menu

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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.kram.pagingsample.R
import ru.kram.pagingsample.designsystem.bottomsheet.BaseBottomSheet
import ru.kram.pagingsample.designsystem.theme.Colors
import ru.kram.pagingsample.designsystem.theme.PagingSampleTheme
import ru.kram.pagingsample.ui.navigation.menu.MenuComponent

@Composable
fun MenuScreen(
    component: MenuComponent,
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<MenuViewModel>()
    val isSettingsExpanded = remember { mutableStateOf(false) }

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
                text = stringResource(R.string.settings),
                modifier = Modifier.fillMaxWidth(),
                onClick = { isSettingsExpanded.value = true }
            )

            PrimaryActionButton(
                text = stringResource(R.string.paging3_title),
                modifier = Modifier.fillMaxWidth(),
                onClick = { component.onMenuItemSelected(MenuComponent.MenuItem.PAGING3) }
            )

            PrimaryActionButton(
                text = stringResource(R.string.custom_pager_title),
                modifier = Modifier.fillMaxWidth(),
                onClick = { component.onMenuItemSelected(MenuComponent.MenuItem.CUSTOMPAGER) }
            )

            PrimaryActionButton(
                text = stringResource(R.string.both_title),
                modifier = Modifier.fillMaxWidth(),
                onClick = { component.onMenuItemSelected(MenuComponent.MenuItem.BOTH) }
            )
        }
    }

    SettingsBottomSheet(
        isExpanded = isSettingsExpanded,
        onInitServer = viewModel::startInitServer
    )
}

@Composable
private fun SettingsBottomSheet(
    isExpanded: MutableState<Boolean>,
    onInitServer: () -> Unit
) {
    if (!isExpanded.value) return

    BaseBottomSheet(
        onDismissRequest = {
            isExpanded.value = false
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            PrimaryActionButton(
                text = stringResource(R.string.init_server),
                onClick = onInitServer
            )
        }
    }
}

@Preview
@Composable
private fun MenuScreenPreview() {
    PagingSampleTheme {
        MenuScreen(
            component = object : MenuComponent {
                override val onMenuItemSelected: (MenuComponent.MenuItem) -> Unit = {}
            }
        )
    }
}
