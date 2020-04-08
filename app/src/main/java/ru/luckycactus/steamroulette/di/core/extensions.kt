package ru.luckycactus.steamroulette.di.core

import android.app.Activity
import androidx.fragment.app.Fragment
import ru.luckycactus.steamroulette.di.common.BaseAppComponent

val <T : Any> ComponentOwner<T>.component: T
    get() = InjectionManager.bindComponent(this)

val Fragment.appComponent: BaseAppComponent
    get() = InjectionManager.findComponent()

val Activity.appComponent: BaseAppComponent
    get() = InjectionManager.findComponent()

inline fun <reified T> Fragment.findComponent(): T = InjectionManager.findComponent()