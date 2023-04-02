package ru.luckycactus.steamroulette.presentation.ui.compose.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat

@Composable
fun adaptiveIconResource(@DrawableRes id: Int): ImageBitmap {
    val context = LocalContext.current
    return remember {
        val drawable = ResourcesCompat.getDrawable(context.resources, id, context.theme)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap.asImageBitmap()
    }
}

@Composable
@ReadOnlyComposable
fun quantityStringResource(@PluralsRes id: Int, quantity: Int, vararg args: Any): String {
    val res = LocalContext.current.resources
    return res.getQuantityString(id, quantity, *args)
}