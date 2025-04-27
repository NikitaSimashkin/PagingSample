package ru.kram.pagingsample.ui.navigation.menu

import com.arkivanov.decompose.ComponentContext

interface MenuComponent {
    val onMenuItemSelected: (MenuItem) -> Unit

    enum class MenuItem {
        PAGING3, CUSTOMPAGER
    }
}

class MenuComponentImpl(
    private val componentContext: ComponentContext,
    private val onPagingClick: () -> Unit,
    private val onCustomPagerClick: () -> Unit,
) : MenuComponent, ComponentContext by componentContext {
    override val onMenuItemSelected: (MenuComponent.MenuItem) -> Unit = {
        when(it) {
            MenuComponent.MenuItem.PAGING3 -> onPagingClick()
            MenuComponent.MenuItem.CUSTOMPAGER -> onCustomPagerClick()
        }
    }
}

