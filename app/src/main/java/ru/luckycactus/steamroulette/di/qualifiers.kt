package ru.luckycactus.steamroulette.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class AppCoScope

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class Interceptors

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class NetworkInterceptors