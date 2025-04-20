package ru.kram.pagingsample.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.kram.pagingsample.data.paging.FilmsPagingSource
import ru.kram.pagingsample.data.paging.FilmsRemoteMediator
import ru.kram.pagingsample.ui.paging3.Paging3ViewModel

val paging3Module = module {

    single {
        FilmsPagingSource.Factory(
            basePageSize = Paging3ViewModel.PAGE_SIZE,
            filmLocalDao = get(),
        )
    }

    single {
        FilmsRemoteMediator(
            filmLocalDao = get(),
            filmsRemoteDataSource = get(),
            basePageSize = Paging3ViewModel.PAGE_SIZE
        )
    }

    viewModel {
        Paging3ViewModel(
            filmsPagingSourceFactory = get(),
            filmsRemoteMediator = get(),
            filmLocalDatabase = get(),
            filmsRepository = get(),
        )
    }
}