package ru.luckycactus.steamroulette.domain.common

abstract class Mapper<From, To> {

    abstract fun mapFrom(from: From): To

    fun mapFrom(from: Collection<From>): List<To> {
        return from.map { mapFrom(it) }
    }
}