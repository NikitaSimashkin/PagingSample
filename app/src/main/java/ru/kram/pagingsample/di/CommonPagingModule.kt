package ru.kram.pagingsample.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.kram.pagingsample.core.ServerDatabaseTransactionHelper
import ru.kram.pagingsample.data.FilmsRepository
import ru.kram.pagingsample.data.db.local.FilmLocalDao
import ru.kram.pagingsample.data.db.local.FilmLocalDataSource
import ru.kram.pagingsample.data.db.local.FilmLocalDatabase
import ru.kram.pagingsample.data.remote.FilmsRemoteDataSource

val commonPagingModule = module {

    single<FilmLocalDatabase> {
        Room.databaseBuilder(
            androidContext(),
            FilmLocalDatabase::class.java,
            FilmLocalDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        FilmsRemoteDataSource(
            roomTransactionHelper = get<ServerDatabaseTransactionHelper>(),
            filmPersistentDao = get(),
            userFilmDao = get(),
        )
    }

    single<FilmLocalDao> {
        get<FilmLocalDatabase>().filmLocalDao()
    }

    single {
        FilmLocalDataSource(
            filmLocalDao = get(),
            dispatchers = get(),
            context = androidContext(),
        )
    }

    single {
        FilmsRepository(
            filmsRemoteDataSource = get(),
            filmLocalDataSource = get(),
            dispatchers = get(),
        )
    }
}