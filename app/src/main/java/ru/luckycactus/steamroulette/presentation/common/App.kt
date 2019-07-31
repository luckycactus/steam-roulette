package ru.luckycactus.steamroulette.presentation.common

import android.app.Application
import ru.luckycactus.steamroulette.di.DI

//todo Не перезаписывать игры при обновлении
//todo Возможность скрыть игры навсегда
//todo prefetch
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        DI.init(this)
    }
}