package ru.kram.pagingsample.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    data object Menu : Screen()

    @Serializable
    data object Paging3 : Screen()

    @Serializable
    data object CustomPager : Screen()

    @Serializable
    data object Both : Screen()
}