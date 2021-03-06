package ru.luckycactus.steamroulette.data.repositories.app

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import ru.luckycactus.steamroulette.data.core.int
import ru.luckycactus.steamroulette.data.utils.BroadcastReceiverAdapter
import ru.luckycactus.steamroulette.domain.app.AppRepository
import javax.inject.Inject
import javax.inject.Named

@Reusable
class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("app") private val appPrefs: SharedPreferences
) : AppRepository {

    override var lastVersion by appPrefs.int(0, "last_version")

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