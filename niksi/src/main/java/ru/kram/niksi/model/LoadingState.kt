package ru.kram.niksi.model

sealed interface LoadingState {

    data object Start : LoadingState

    data object End : LoadingState

    data object Both : LoadingState

    data object None : LoadingState
}