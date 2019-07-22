package ru.luckycactus.steamroulette.domain.exception

class NetworkConnectionException(cause: Throwable? = null) : Exception(cause)

class ServerException(cause: Throwable? = null) : Exception(cause)

class ApiException(cause: Throwable? = null) : Exception(cause)

class InvalidVanityException(
    private val vanity: String
) : Exception()

class InvalidSteamIdFormatException : Exception()

