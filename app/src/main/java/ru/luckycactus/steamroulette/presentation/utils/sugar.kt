package ru.luckycactus.steamroulette.presentation.utils


fun <T> lazyNonThreadSafe(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)