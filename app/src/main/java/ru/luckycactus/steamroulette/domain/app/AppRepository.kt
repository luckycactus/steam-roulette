package ru.luckycactus.steamroulette.domain.app

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.core.Event

interface AppRepository {
    var lastVersion: Int
    val currentVersion: Int

    fun observeSystemLocaleChanges(): LiveData<Event<Unit>>
}