package ru.luckycactus.steamroulette.data.repositories.app

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import dagger.Reusable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.core.int
import ru.luckycactus.steamroulette.data.utils.BroadcastReceiverAdapter
import ru.luckycactus.steamroulette.di.ForApplication
import ru.luckycactus.steamroulette.di.Identified
import ru.luckycactus.steamroulette.domain.app.AppRepository
import javax.inject.Inject

@Reusable
class AppRepositoryImpl @Inject constructor(
    @ForApplication private val context: Context,
    @Identified(R.id.appPrefs) private val appPrefs: SharedPreferences
) : AppRepository {

    override var lastVersion by appPrefs.int("last_version", 0)

    override val currentVersion: Int
        get() = context.packageManager.getPackageInfo(context.packageName, 0).versionCode

    override val currentVersionName: String
        get() = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    override fun observeSystemLocaleChanges() = callbackFlow {
        val receiver = BroadcastReceiverAdapter { offer(Unit) }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_LOCALE_CHANGED))
        awaitClose { context.unregisterReceiver(receiver) }
    }
}