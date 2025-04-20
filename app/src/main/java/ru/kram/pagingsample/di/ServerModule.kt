package ru.kram.pagingsample.di

import android.content.Context
import androidx.room.Room
import com.github.javafaker.Faker
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.kram.pagingsample.core.RoomTransactionHelper
import ru.kram.pagingsample.core.ServerDatabaseTransactionHelper
import ru.kram.pagingsample.data.db.server.FilmServerDatabase
import ru.kram.pagingsample.data.db.PersistentTableLoader
import ru.kram.pagingsample.data.db.ServerDatabaseInitializer

val serverModule = module {

    single {
        getServerDatabase(androidContext())
    }

    single {
        get<FilmServerDatabase>().filmPersistentDao()
    }

    single {
        get<FilmServerDatabase>().userFilmDao()
    }

    single {
        Faker()
    }

    single {
        Gson()
    }

    single {
        ServerDatabaseTransactionHelper(
            get<FilmServerDatabase>(),
            get()
        )
    }

    singleOf(::PersistentTableLoader)
    singleOf(::ServerDatabaseInitializer)
}

private fun getServerDatabase(
    context: Context
): FilmServerDatabase {
    return Room.databaseBuilder(
        context,
        FilmServerDatabase::class.java,
        FilmServerDatabase.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()
}