package ru.luckycactus.steamroulette.domain.core

import dagger.Reusable
import javax.inject.Inject

@Reusable
class SystemClock @Inject constructor(
) : Clock {
    override fun currentTimeMillis() = System.currentTimeMillis()
}