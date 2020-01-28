package ru.luckycactus.steamroulette.data.repositories.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LocaleChangedBroadcastReceiver(
    private val onLocaleChanged: () -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        onLocaleChanged()
    }
}