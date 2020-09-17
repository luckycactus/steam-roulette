package ru.luckycactus.steamroulette.presentation.utils

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory

object PlayUtils {

    suspend fun reviewApp(activity: AppCompatActivity) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            val reviewInfo = manager.requestReview()
            manager.launchReview(activity, reviewInfo)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + activity.packageName)
                )
            )
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + activity.packageName)
                )
            )
        }

    }
}