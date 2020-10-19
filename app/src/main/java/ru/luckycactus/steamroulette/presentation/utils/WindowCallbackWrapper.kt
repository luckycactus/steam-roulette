package ru.luckycactus.steamroulette.presentation.utils

import android.view.Window

open class WindowCallbackWrapper(
    val wrapped: Window.Callback
) : Window.Callback by wrapped