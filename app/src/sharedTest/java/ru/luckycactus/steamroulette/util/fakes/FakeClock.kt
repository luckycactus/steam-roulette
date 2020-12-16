package ru.luckycactus.steamroulette.util.fakes

import ru.luckycactus.steamroulette.domain.core.Clock
import kotlin.time.Duration

class FakeClock(
    private var current: Long = System.currentTimeMillis()
) : Clock {

    override fun currentTimeMillis() = current

    fun advanceTimeBy(offset: Duration) {
        current += offset.toLongMilliseconds()
    }

    fun set(millis: Long) {
        current = millis
    }

    fun setToSystem() {
        set(System.currentTimeMillis())
    }
}