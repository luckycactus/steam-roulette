package ru.luckycactus.steamroulette.di

import android.app.Application

object DI {

    fun init(app: Application) {
        AppModule.init(app)
    }
}