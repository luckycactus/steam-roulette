package ru.luckycactus.steamroulette.domain.core

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Error(val message: String) : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()

    companion object {
        val success =
            Success(Unit)
    }
}