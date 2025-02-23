package ru.kram.pagingsample.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.kram.pagingsample.ui.menu.MenuViewModel

val menuScreenModule = module {

    viewModel {
        MenuViewModel(get())
    }
}