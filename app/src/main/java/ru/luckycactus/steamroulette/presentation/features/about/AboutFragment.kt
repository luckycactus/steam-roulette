package ru.luckycactus.steamroulette.presentation.features.about

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.addSystemTopPadding
import ru.luckycactus.steamroulette.presentation.utils.extensions.observe
import ru.luckycactus.steamroulette.presentation.utils.extensions.setDrawableColorFromAttribute

@AndroidEntryPoint
class AboutFragment : BaseFragment() {
    private val viewModel: AboutViewModel by viewModels()

    override val layoutResId = R.layout.fragment_about

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.addSystemTopPadding()

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        tvVersion.text = viewModel.version

        tvSourceCode.setDrawableColorFromAttribute(R.attr.colorOnSurface)
        tvSourceCode.setOnClickListener {
            viewModel.onSourceCodeClick()
        }

        tvUsedLibraries.setDrawableColorFromAttribute(R.attr.colorOnSurface)
        tvUsedLibraries.setOnClickListener {
            viewModel.onUsedLibrariesClick()
        }

        tvPrivacyPolicy.setDrawableColorFromAttribute(R.attr.colorOnSurface)
        tvPrivacyPolicy.setOnClickListener {
            viewModel.onPrivacyPolicyClick()
        }

        tvRateApp.setOnClickListener {
            analytics.logClick("Review app")
            (activity as MainActivity).reviewApp()
        }

        setupIconDragging()
        setupIconClick()

        lifecycleScope.launch {
            delay(100)
            ivIcon.performClick()
        }

        observe(viewModel.appRated) {
            tvRateApp.setBackgroundResource(
                if (it)
                    R.drawable.surface_selectable_item_background
                else
                    R.drawable.primary_selectable_item_background
            )
        }
    }

    private fun setupIconClick() {
        val scaleX = createIconScaleSpringAnimation(DynamicAnimation.SCALE_X)
        val scaleY = createIconScaleSpringAnimation(DynamicAnimation.SCALE_Y)
        ivIcon.setOnClickListener {
            scaleX.animateToFinalPosition(ICON_CLICK_SCALE)
            scaleY.animateToFinalPosition(ICON_CLICK_SCALE)
        }
    }

    private fun createIconScaleSpringAnimation(property: DynamicAnimation.ViewProperty) =
        ivIcon.let {
            SpringAnimation(ivIcon, property, ICON_CLICK_SCALE).apply {
                addUpdateListener { _, _, _ ->
                    if (property.getValue(it) >= ICON_CLICK_SCALE) {
                        animateToFinalPosition(1f)
                    }
                }
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
            }
        }

    private fun setupIconDragging() {
        ivIcon.setOnTouchListener(
            object : View.OnTouchListener {
                var startX: Float = 0f
                var startY: Float = 0f
                var startTranslationX = 0f
                var startTranslationY = 0f
                var click = false

                val animX = createAnimation(DynamicAnimation.TRANSLATION_X)
                val animY = createAnimation(DynamicAnimation.TRANSLATION_Y)

                private fun createAnimation(property: DynamicAnimation.ViewProperty) =
                    SpringAnimation(ivIcon, property, 0f).apply {
                        spring.stiffness = SpringForce.STIFFNESS_LOW
                    }

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    var handled = false
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            click = true
                            startX = event.rawX
                            startY = event.rawY
                            startTranslationX = ivIcon.translationX
                            startTranslationY = ivIcon.translationY
                            animateToCurrentTouchPosition(event)
                            handled = true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            click = false
                            animateToCurrentTouchPosition(event)
                            handled = true
                        }
                        MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                            if (click)
                                v.performClick()
                            animateToPosition(0f, 0f)
                            handled = true
                        }
                    }
                    return handled
                }

                private fun animateToCurrentTouchPosition(event: MotionEvent) {
                    animateToPosition(
                        startTranslationX + event.rawX - startX,
                        startTranslationY + event.rawY - startY
                    )
                }

                private fun animateToPosition(x: Float, y: Float) {
                    animX.animateToFinalPosition(x)
                    animY.animateToFinalPosition(y)
                }
            })
    }

    companion object {
        private const val ICON_CLICK_SCALE = 1.3f
        fun newInstance() = AboutFragment()
    }
}