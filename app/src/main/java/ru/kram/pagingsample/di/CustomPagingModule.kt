package ru.kram.pagingsample.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.kram.pagingsample.ui.custompager.jumpable.JumpablePagerViewModel
import ru.kram.pagingsample.ui.custompager.filterable.FilterablePagerViewModel

val customPagingModule = module {

    viewModel {
        FilterablePagerViewModel(
            filmsRepository = get(),
        )
    }

    viewModel {
        JumpablePagerViewModel(
            filmsRepository = get(),
        )
    }
}