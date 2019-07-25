package ru.luckycactus.steamroulette.presentation


fun <T> lazyNonThreadSafe(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)