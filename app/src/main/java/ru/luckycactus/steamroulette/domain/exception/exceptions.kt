package ru.luckycactus.steamroulette.domain.exception

class NetworkConnectionException(cause: Throwable? = null) : Exception(cause)

class ServerException(cause: Throwable? = null) : Exception(cause)

class VanityNotFoundException(val vanity: String) : Exception()

class SteamIdNotFoundException(val steamId: String): Exception()

class InvalidSteamIdFormatException : Exception()

