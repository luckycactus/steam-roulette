package ru.luckycactus.steamroulette.domain.core

interface Clock {
    fun currentTimeMillis(): Long
}