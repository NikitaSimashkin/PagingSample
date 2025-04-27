package ru.kram.pagingsample.ui.navigation

import kotlinx.serialization.Serializable
import ru.kram.pagingsample.ui.navigation.menu.CustomPagerScreenType

@Serializable
sealed class Screen {

    @Serializable
    data object Menu : Screen()

    @Serializable
    data object Paging3 : Screen()

    @Serializable
    data object CustomPagerMenu : Screen()

    @Serializable
    data class CustomPager(val screen: CustomPagerScreenType) : Screen()
}