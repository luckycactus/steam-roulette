package ru.luckycactus.steamroulette.presentation.utils.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import jp.wasabeef.glide.transformations.BitmapTransformation
import jp.wasabeef.glide.transformations.internal.FastBlur
import java.security.MessageDigest


class CoverBlurTransformation(
    private val radius: Int,
    private val sampling: Int,
    private val bias: Float
) : BitmapTransformation() {

    //private val rs: RenderScript //todo

    init {
        check(!(bias < 0f || bias > 1f)) { "bias should be in 0..1 range" }
        //rs = RenderScript.create(context)
    }

    override fun transform(
        context: Context,
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        if (toTransform.width < toTransform.height) //todo
            return toTransform
        val bitmap = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            //flags = Paint.FILTER_BITMAP_FLAG
        }

        //if (sampling > 1) {
        val scaledWidth = outWidth / sampling
        val scaledHeight = outHeight / sampling
        var blurBitmap = pool.get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888).apply {
            density = toTransform.density
        }

        with(Canvas(blurBitmap)) {
            density = toTransform.density
            scale(1f, outHeight / toTransform.height.toFloat())
            scale(1f / sampling, 1f / sampling)
            drawBitmap(toTransform, 0f, 0f, paint)
        }
        blurBitmap = FastBlur.blur(blurBitmap, radius, true)
        //blurBitmap = blur(blurBitmap, radius.toFloat())
        canvas.save()
        canvas.scale(outWidth.toFloat() / scaledWidth, outHeight.toFloat() / scaledHeight)
        canvas.drawBitmap(blurBitmap, 0f, 0f, paint)
        canvas.restore()
        //} else {
//            canvas.drawBitmap()
        //}
        pool.put(blurBitmap)

        var y = bias * outHeight - toTransform.height / 2
        y = y.coerceIn(0f, outHeight - toTransform.height / 2f)
        canvas.drawBitmap(toTransform, 0f, y, paint)

        return bitmap
    }

//    fun blur(bitmap: Bitmap, radius: Float): Bitmap {
//
//        // Allocate memory for Renderscript to work with
//        val input = Allocation.createFromBitmap(
//            rs,
//            bitmap,
//            Allocation.MipmapControl.MIPMAP_FULL,
//            Allocation.USAGE_SHARED
//        )
//        val output = Allocation.createTyped(rs, input.type)
//
//        // Load up an instance of the specific script that we want to use.
//        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
//        script.setInput(input)
//
//        // Set the blur radius
//        script.setRadius(radius)
//
//        // Start the ScriptIntrinisicBlur
//        script.forEach(output)
//
//        // Copy the output to the blurred bitmap
//        output.copyTo(bitmap)
//
//        return bitmap
//    }


    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + radius + sampling).toByteArray(Key.CHARSET))
    }

    override fun equals(other: Any?): Boolean {
        return other is CoverBlurTransformation
                && radius == other.radius
                && sampling == other.sampling
                && bias.toRawBits() == other.bias.toRawBits()
    }

    override fun hashCode(): Int {
        var result = radius
        result = 31 * result + sampling
        result = 31 * result + bias.hashCode()
        return result
    }


    companion object {
        private const val VERSION = 1
        private const val ID = "ru.luckycactus.steamroulette.CoverBlurTransformation.$VERSION"
    }
}