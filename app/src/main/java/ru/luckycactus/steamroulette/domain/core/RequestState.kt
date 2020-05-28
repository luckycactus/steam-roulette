package ru.luckycactus.steamroulette.domain.core

sealed class RequestState<out T> {
    object Loading : RequestState<Nothing>()
    data class Error(val message: String) : RequestState<Nothing>()
    data class Success<out T>(val data: T) : RequestState<T>()

    companion object {
        val success = Success(Unit)
    }
}