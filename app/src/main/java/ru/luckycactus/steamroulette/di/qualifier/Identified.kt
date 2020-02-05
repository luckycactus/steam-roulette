package ru.luckycactus.steamroulette.di.qualifier

import androidx.annotation.IdRes
import javax.inject.Qualifier

/**
 * Int-based [qualifier][Qualifier].
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Identified(
    @IdRes val id: Int
)