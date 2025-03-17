package ru.kram.pagingsample.ui.navigation

import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import ru.kram.pagingsample.core.viewModelStoreOwner
import ru.kram.pagingsample.ui.navigation.menu.CustomPagerScreenType
import ru.kram.pagingsample.ui.navigation.menu.CustomPagersMenuComponent
import ru.kram.pagingsample.ui.navigation.menu.CustomPagersMenuComponentImpl
import ru.kram.pagingsample.ui.navigation.menu.MenuComponent
import ru.kram.pagingsample.ui.navigation.menu.MenuComponentImpl
import ru.kram.pagingsample.ui.navigation.menu.screenObject

interface RootComponent {
    val childStack: Value<ChildStack<Screen, Child>>

    fun onMenuItemClicked()

    sealed class Child {
        data class Menu(val component: MenuComponent) : Child()
        data class Paging3(
            val component: Paging3Component,
            val viewModelStoreOwner: ViewModelStoreOwner
        ) : Child()
        data class CustomPagerMenu(
            val component: CustomPagersMenuComponent,
        ) : Child()
        data class Both(
            val component: BothComponentImpl,
            val viewModelStoreOwner: ViewModelStoreOwner,
        ) : Child()
        data class CustomPager(
            val screen: CustomPagerScreenType,
            val viewModelStoreOwner: ViewModelStoreOwner,
        ) : Child()
    }
}

class RootComponentImpl(
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Screen>()

    override val childStack = childStack(
        source = navigation,
        serializer = Screen.serializer(),
        initialConfiguration = Screen.Menu,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(
        screen: Screen,
        componentContext: ComponentContext
    ): RootComponent.Child =
        when (screen) {
            Screen.Menu -> RootComponent.Child.Menu(
                MenuComponentImpl(
                    componentContext = componentContext,
                    onPagingClick = { navigation.push(Screen.Paging3) },
                    onCustomPagerClick = { navigation.push(Screen.CustomPagerMenu) },
                    onBothClick = { navigation.push(Screen.Both) },
                )
            )
            Screen.Paging3 -> {
                val component = Paging3ComponentImpl(componentContext)
                RootComponent.Child.Paging3(
                    component = component,
                    viewModelStoreOwner = component.viewModelStoreOwner()
                )
            }
            Screen.CustomPagerMenu -> RootComponent.Child.CustomPagerMenu(
                CustomPagersMenuComponentImpl(componentContext) {
                    navigation.push(it.screenObject)
                }
            )
            Screen.Both -> RootComponent.Child.Both(
                BothComponentImpl(componentContext),
                componentContext.viewModelStoreOwner(),
            )
            is Screen.CustomPager -> RootComponent.Child.CustomPager(
                screen = screen.screen,
                viewModelStoreOwner = componentContext.viewModelStoreOwner(),
            )
        }

    override fun onMenuItemClicked() {
        navigation.push(Screen.Menu)
    }
}