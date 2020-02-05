package ru.luckycactus.steamroulette.di.common

import android.app.Activity
import androidx.fragment.app.Fragment
import ru.luckycactus.steamroulette.di.core.ComponentOwner
import ru.luckycactus.steamroulette.di.core.InjectionManager

val <T : Any> ComponentOwner<T>.component: T
    get() = InjectionManager.bindComponent(this)

val Fragment.appComponent: AppComponent
    get() = InjectionManager.findComponent()

val Activity.appComponent: AppComponent
    get() = InjectionManager.findComponent()

inline fun <reified T> Fragment.findComponent(): T = InjectionManager.findComponent()