package ru.luckycactus.steamroulette.presentation

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

fun View.visibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun <T> MutableLiveData<T>.startWith(item: T): MutableLiveData<T> {
    value = item
    return this
}

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, body: (T) -> Unit) {
    liveData.observe(this, Observer { body(it) })
}

fun Boolean?.nullIfFalse() = if (this == false) null else this