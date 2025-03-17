package ru.kram.pagingsample.ui.navigation.menu

import com.arkivanov.decompose.ComponentContext
import ru.kram.pagingsample.ui.navigation.Screen

enum class CustomPagerScreenType {
    SIMPLE_PAGER,
    SIMPLE_PAGER_WITH_LOADING_STATE,
}

val CustomPagerScreenType.screenObject: Screen get() = Screen.CustomPager(this)

interface CustomPagersMenuComponent {
    fun onPagerScreenSelected(menuItem: CustomPagerScreenType)
}

class CustomPagersMenuComponentImpl(
    componentContext: ComponentContext,
    private val openPagerScreen: (CustomPagerScreenType) -> Unit,
) : CustomPagersMenuComponent, ComponentContext by componentContext {
    override fun onPagerScreenSelected(menuItem: CustomPagerScreenType) = openPagerScreen(menuItem)
}