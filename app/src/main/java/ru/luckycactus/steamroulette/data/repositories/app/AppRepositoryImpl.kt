package ru.luckycactus.steamroulette.data.repositories.app

import android.content.*
import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.core.int
import ru.luckycactus.steamroulette.data.utils.BroadcastReceiverAdapter
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.di.qualifier.Identified
import ru.luckycactus.steamroulette.domain.core.Event
import ru.luckycactus.steamroulette.domain.app.AppRepository
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    @ForApplication private val context: Context,
    @Identified(R.id.appPrefs) private val appPrefs: SharedPreferences
) : AppRepository {

    override var lastVersion by appPrefs.int("last_version", -1)

    override val currentVersion: Int
        get() = context.packageManager.getPackageInfo(context.packageName, 0).versionCode

    override fun observeSystemLocaleChanges(): LiveData<Event<Unit>> {
        return LocaleChangeLiveData()
    }

    private inner class LocaleChangeLiveData : LiveData<Event<Unit>>() {
        private val receiver = BroadcastReceiverAdapter { updateValue() }
        private val filter = IntentFilter(Intent.ACTION_LOCALE_CHANGED)

        override fun onActive() {
            context.registerReceiver(receiver, filter)
        }

        override fun onInactive() {
            context.unregisterReceiver(receiver)
        }

        private fun updateValue() {
            value = Event(Unit)
        }
    }
}