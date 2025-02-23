package ru.kram.pagingsample.di

import org.koin.dsl.module
import ru.kram.pagingsample.core.CatDispatchers
import ru.kram.pagingsample.core.DefaultCatDispatchers

val appModule = module {
    single<CatDispatchers> {
        DefaultCatDispatchers()
    }
}