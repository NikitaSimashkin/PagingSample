package ru.kram.pagingsample.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.kram.pagingsample.core.ServerDatabaseTransactionHelper
import ru.kram.pagingsample.data.CatsRepository
import ru.kram.pagingsample.data.db.local.CatLocalDao
import ru.kram.pagingsample.data.db.local.CatLocalDatabase
import ru.kram.pagingsample.data.remote.CatsRemoteDataSource

val commonPagingModule = module {

    single<CatLocalDatabase> {
        Room.databaseBuilder(
            androidContext(),
            CatLocalDatabase::class.java,
            CatLocalDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        CatsRemoteDataSource(
            roomTransactionHelper = get<ServerDatabaseTransactionHelper>(),
            catPersistentDao = get(),
            userCatDao = get(),
        )
    }

    single<CatLocalDao> {
        get<CatLocalDatabase>().catLocalDao()
    }

    single {
        CatsRepository(
            catsRemoteDataSource = get(),
            catLocalDao = get(),
            dispatchers = get(),
        )
    }
}