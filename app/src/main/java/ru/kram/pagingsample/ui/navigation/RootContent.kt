package ru.kram.pagingsample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import ru.kram.pagingsample.ui.catlist.both.BothScreen
import ru.kram.pagingsample.ui.custompager.CustomPagerScreen
import ru.kram.pagingsample.ui.menu.MenuScreen
import ru.kram.pagingsample.ui.paging3.Paging3Screen

@Composable
fun RootContent(component: RootComponent) {
    Children(component.childStack) {
        when (val instance = it.instance) {
            is RootComponent.Child.Menu -> {
                MenuScreen(instance.component)
            }
            is RootComponent.Child.Paging3 -> {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides instance.viewModelStoreOwner
                ) {
                    Paging3Screen()
                }
            }
            is RootComponent.Child.CustomPager -> {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides instance.viewModelStoreOwner
                ) {
                    CustomPagerScreen()
                }
            }
            is RootComponent.Child.Both -> {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides instance.viewModelStoreOwner
                ) {
                    BothScreen()
                }
            }
        }
    }
}