package ru.kram.pagingsample.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.kram.pagingsample.ui.custompager.simplepager.SimplePagerViewModel
import ru.kram.pagingsample.ui.custompager.simplepagerloading.SimplePagerWithLoadingStateViewModel

val customPagingModule = module {

    viewModel {
        SimplePagerViewModel(
            catsRepository = get(),
            catsRemoteDataSource = get(),
        )
    }

    viewModel {
        SimplePagerWithLoadingStateViewModel(
            catsRepository = get(),
            catsRemoteDataSource = get(),
        )
    }
}