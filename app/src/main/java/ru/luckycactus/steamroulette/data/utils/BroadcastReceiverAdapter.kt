package ru.luckycactus.steamroulette.data.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BroadcastReceiverAdapter(
    private val onReceive: (intent: Intent) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        onReceive(intent)
    }
}