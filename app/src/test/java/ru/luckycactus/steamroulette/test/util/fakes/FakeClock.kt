package ru.luckycactus.steamroulette.test.util.fakes

import ru.luckycactus.steamroulette.domain.core.Clock
import kotlin.time.Duration

class FakeClock(
    private var current: Long = System.currentTimeMillis()
) : Clock {

    override fun currentTimeMillis() = current

    fun offset(offset: Duration) {
        current += offset.toLongMilliseconds()
    }

    fun set(millis: Long) {
        current = millis
    }
}