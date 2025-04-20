package ru.kram.pagingsample.di

import org.koin.dsl.module
import ru.kram.pagingsample.core.FilmDispatchers
import ru.kram.pagingsample.core.DefaultFilmDispatchers

val appModule = module {
    single<FilmDispatchers> {
        DefaultFilmDispatchers()
    }
}