/*
 * based on code by Yaser Rajabi https://github.com/yrajabi
 * https://gist.github.com/yrajabi/5776f4ade5695009f87ce7fcbc08078f
 */
package ru.luckycactus.steamroulette.presentation.utils.glide

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.text.Html
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import java.lang.ref.WeakReference

class GlideImageGetter(
    textView: TextView,
    private val matchParentWidth: Boolean = false,
    private val imagesHandler: HtmlImagesHandler? = null,
    densityAware: Boolean = false
) : Html.ImageGetter {

    private var container = WeakReference(textView)
    private var density = if (densityAware) textView.resources.displayMetrics.density else 1f

    override fun getDrawable(source: String?): Drawable {
        imagesHandler?.addImage(source)

        return container.get()?.let {
            val drawable = BitmapDrawablePlaceholder(it.resources)
            it.post {
                Glide.with(it)
                    .asBitmap()
                    .load(source)
                    .into(drawable)
            }
            drawable
        } ?: ShapeDrawable() //empty drawable
    }

    private inner class BitmapDrawablePlaceholder(
        resources: Resources
    ) : BitmapDrawable(
        resources,
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    ), Target<Bitmap> {
        private var drawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            drawable?.run { draw(canvas) }
        }

        private fun setDrawable(drawable: Drawable) {
            container.get()?.let {
                this.drawable = drawable
                val drawableWidth = (drawable.intrinsicWidth * density).toInt()
                val drawableHeight = (drawable.intrinsicHeight * density).toInt()
                val maxWidth: Int = it.measuredWidth
                if (drawableWidth > maxWidth || matchParentWidth) {
                    val calculatedHeight = maxWidth * drawableHeight / drawableWidth
                    drawable.setBounds(0, 0, maxWidth, calculatedHeight)
                    setBounds(0, 0, maxWidth, calculatedHeight)
                } else {
                    drawable.setBounds(0, 0, drawableWidth, drawableHeight)
                    setBounds(0, 0, drawableWidth, drawableHeight)
                }
                it.text = it.text
            }
        }

        override fun onLoadStarted(placeholderDrawable: Drawable?) {
            placeholderDrawable?.let { setDrawable(it) }
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            errorDrawable?.let { setDrawable(it) }
        }

        override fun onResourceReady(
            bitmap: Bitmap,
            transition: Transition<in Bitmap>?
        ) {
            container.get()?.let {
                setDrawable(BitmapDrawable(it.resources, bitmap))
            }
        }

        override fun onLoadCleared(placeholderDrawable: Drawable?) {
            placeholderDrawable?.let { setDrawable(it) }
        }

        override fun getSize(cb: SizeReadyCallback) {
            cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
        }

        override fun removeCallback(cb: SizeReadyCallback) {}
        override fun setRequest(request: Request?) {}
        override fun getRequest(): Request? = null

        override fun onStart() {}
        override fun onStop() {}
        override fun onDestroy() {}
    }

    interface HtmlImagesHandler {
        fun addImage(uri: String?)
    }
}