package ru.kram.pagingsample.ui.navigation

import com.arkivanov.decompose.ComponentContext

interface Paging3Component: ComponentContext

class Paging3ComponentImpl(
    private val componentContext: ComponentContext,
) : Paging3Component, ComponentContext by componentContext