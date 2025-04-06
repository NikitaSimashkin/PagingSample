package ru.kram.pagingsample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import ru.kram.pagingsample.ui.catlist.both.BothScreen
import ru.kram.pagingsample.ui.catlist.custompagermenu.CustomPagersMenuScreen
import ru.kram.pagingsample.ui.custompager.simplepager.SimplePagerScreen
import ru.kram.pagingsample.ui.custompager.jumpablepager.JumpablePagerScreen
import ru.kram.pagingsample.ui.custompager.simplepagerloading.SimplePagerWithLoadingStateScreen
import ru.kram.pagingsample.ui.menu.MenuScreen
import ru.kram.pagingsample.ui.navigation.menu.CustomPagerScreenType
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
            is RootComponent.Child.CustomPagerMenu -> {
                CustomPagersMenuScreen(instance.component)
            }
            is RootComponent.Child.Both -> {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides instance.viewModelStoreOwner
                ) {
                    BothScreen()
                }
            }
            is RootComponent.Child.CustomPager -> {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides instance.viewModelStoreOwner
                ) {
                    when (instance.screen) {
                        CustomPagerScreenType.SIMPLE_PAGER -> {
                            SimplePagerScreen()
                        }

                        CustomPagerScreenType.SIMPLE_PAGER_WITH_LOADING_STATE -> {
                            SimplePagerWithLoadingStateScreen()
                        }

                        CustomPagerScreenType.JUMPABLE_PAGER -> {
                            JumpablePagerScreen()
                        }
                    }
                }
            }
        }
    }
}