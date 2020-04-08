package ru.luckycactus.steamroulette.domain.common

class VanityNotFoundException(val vanity: String) : Exception()

class SteamIdNotFoundException(val steamId: String): Exception()

class GetGameStoreInfoException: Exception()

class InvalidSteamIdFormatException : Exception()

class GetOwnedGamesPrivacyException: Exception()

class MissingOwnedGamesException: Exception()