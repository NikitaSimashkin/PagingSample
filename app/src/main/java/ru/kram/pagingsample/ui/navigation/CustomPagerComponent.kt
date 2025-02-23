package ru.kram.pagingsample.ui.navigation

import com.arkivanov.decompose.ComponentContext

interface CustomPagerComponent

class CustomPagerComponentImpl(
    componentContext: ComponentContext
) : CustomPagerComponent, ComponentContext by componentContext