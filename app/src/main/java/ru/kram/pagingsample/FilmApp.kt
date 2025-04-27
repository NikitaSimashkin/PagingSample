package ru.kram.pagingsample

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.kram.pagingsample.di.appModule
import ru.kram.pagingsample.di.commonPagingModule
import ru.kram.pagingsample.di.customPagingModule
import ru.kram.pagingsample.di.menuScreenModule
import ru.kram.pagingsample.di.paging3Module
import ru.kram.pagingsample.di.serverModule
import timber.log.Timber

class FilmApp: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        startKoin {
            androidContext(this@FilmApp)
            modules(
                appModule,
                serverModule,
                menuScreenModule,
                paging3Module,
                commonPagingModule,
                customPagingModule,
            )
        }
    }
}