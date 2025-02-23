package ru.kram.pagingsample.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.kram.pagingsample.data.paging.CatsPagingSource
import ru.kram.pagingsample.data.paging.CatsRemoteMediator
import ru.kram.pagingsample.ui.paging3.Paging3ViewModel

val paging3Module = module {

    single {
        CatsPagingSource.Factory(get())
    }

    single {
        CatsRemoteMediator(
            catLocalDao = get(),
            catsRemoteDataSource = get(),
        )
    }

    viewModel {
        Paging3ViewModel(
            catsPagingSourceFactory = get(),
            catsRemoteMediator = get(),
            catLocalDatabase = get(),
            catsRepository = get(),
        )
    }
}