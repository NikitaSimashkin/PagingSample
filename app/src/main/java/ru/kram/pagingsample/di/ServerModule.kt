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
import ru.kram.pagingsample.data.db.server.CatServerDatabase
import ru.kram.pagingsample.data.db.PersistentTableLoader
import ru.kram.pagingsample.data.db.ServerDatabaseInitializer

val serverModule = module {

    single {
        getServerDatabase(androidContext())
    }

    single {
        get<CatServerDatabase>().catPersistentDao()
    }

    single {
        get<CatServerDatabase>().userCatDao()
    }

    single {
        Faker()
    }

    single {
        Gson()
    }

    single {
        ServerDatabaseTransactionHelper(
            get<CatServerDatabase>(),
            get()
        )
    }

    singleOf(::PersistentTableLoader)
    singleOf(::ServerDatabaseInitializer)
}

private fun getServerDatabase(
    context: Context
): CatServerDatabase {
    return Room.databaseBuilder(
        context,
        CatServerDatabase::class.java,
        CatServerDatabase.DATABASE_NAME
    ).build()
}