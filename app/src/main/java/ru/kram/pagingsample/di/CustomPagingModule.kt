package ru.kram.pagingsample.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.kram.pagingsample.ui.custompager.CustomPagerViewModel

val customPagingModule = module {

    viewModel {
        CustomPagerViewModel(
            catsRepository = get(),
            catsRemoteDataSource = get(),
            catLocalDao = get(),
        )
    }
}