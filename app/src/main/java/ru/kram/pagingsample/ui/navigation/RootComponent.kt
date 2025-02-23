package ru.kram.pagingsample.ui.navigation

import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import ru.kram.pagingsample.core.viewModelStoreOwner

interface RootComponent {
    val childStack: Value<ChildStack<Screen, Child>>

    fun onMenuItemClicked()

    sealed class Child {
        data class Menu(val component: MenuComponent) : Child()
        data class Paging3(
            val component: Paging3Component,
            val viewModelStoreOwner: ViewModelStoreOwner
        ) : Child()
        data class CustomPager(
            val component: CustomPagerComponent,
            val viewModelStoreOwner: ViewModelStoreOwner
        ) : Child()
        data class Both(
            val component: BothComponentImpl,
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
                    onCustomPagerClick = { navigation.push(Screen.CustomPager) },
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
            Screen.CustomPager -> RootComponent.Child.CustomPager(
                CustomPagerComponentImpl(componentContext),
                componentContext.viewModelStoreOwner()
            )
            Screen.Both -> RootComponent.Child.Both(
                BothComponentImpl(componentContext),
                componentContext.viewModelStoreOwner(),
            )
        }

    override fun onMenuItemClicked() {
        navigation.push(Screen.Menu)
    }
}