package ru.luckycactus.steamroulette.presentation.ui.base

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {
    private var disposables: MutableList<Disposable<*>>? = null

    @MainThread
    protected fun <T> observe(liveData: LiveData<T>, body: (T) -> Unit) {
        if (disposables == null)
            disposables = mutableListOf()
        val observer = Observer(body)
        liveData.observeForever(observer)
        disposables!!.add(Disposable(liveData, observer))
    }

    override fun onCleared() {
        super.onCleared()
        disposables?.forEach {
            it.dispose()
        }
    }

    private class Disposable<T>(
        private val liveData: LiveData<T>,
        private val observer: Observer<T>
    ) {
        fun dispose() {
            liveData.removeObserver(observer)
        }
    }
}