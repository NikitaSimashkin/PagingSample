package ru.kram.pagingsample.ui.navigation

import com.arkivanov.decompose.ComponentContext

interface BothComponent: ComponentContext

class BothComponentImpl(
    private val componentContext: ComponentContext
) : BothComponent, ComponentContext by componentContext