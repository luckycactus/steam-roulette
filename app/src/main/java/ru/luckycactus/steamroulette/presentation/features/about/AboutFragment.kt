package ru.luckycactus.steamroulette.presentation.features.about

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.FragmentAboutBinding
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.*

@AndroidEntryPoint
class AboutFragment : BaseFragment(R.layout.fragment_about) {

    private val binding by viewBinding(FragmentAboutBinding::bind)

    private val viewModel: AboutViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.apply {
            addSystemTopPadding()
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }

        tvVersion.text = viewModel.version

        val colorOnSurface = requireContext().getThemeColorOrThrow(R.attr.colorOnSurface)
        listOf(tvSourceCode, tvUsedLibraries, tvPrivacyPolicy, tvRateApp).forEach {
            it.setDrawableColor(colorOnSurface)
        }

        tvSourceCode.setOnClickListener {
            viewModel.onSourceCodeClick()
        }

        tvUsedLibraries.setOnClickListener {
            viewModel.onUsedLibrariesClick()
        }

        tvRateApp.setOnClickListener {
            analytics.logClick("Review app")
            (activity as MainActivity).reviewApp()
        }

        tvPrivacyPolicy.setOnClickListener {
            viewModel.onPrivacyPolicyClick()
        }

        contactSteam.setOnClickListener {
            viewModel.contactDevViaSteam()
        }

        contactTelegram.setOnClickListener {
            viewModel.contactDevViaTelegram()
        }

        setupIconDragging()
        setupIconClick()

        observe(viewModel.appRated) {
            tvRateApp.setBackgroundResource(
                if (it)
                    R.drawable.surface_selectable_item_background
                else
                    R.drawable.primary_selectable_item_background
            )
        }

        viewLifecycleScope.launch {
            delay(100)
            ivIcon.performClick()
        }
    }

    private fun setupIconClick() {
        val scaleX = createIconScaleSpringAnimation(DynamicAnimation.SCALE_X)
        val scaleY = createIconScaleSpringAnimation(DynamicAnimation.SCALE_Y)
        binding.ivIcon.setOnClickListener {
            scaleX.animateToFinalPosition(ICON_CLICK_SCALE)
            scaleY.animateToFinalPosition(ICON_CLICK_SCALE)
        }
    }

    private fun createIconScaleSpringAnimation(property: DynamicAnimation.ViewProperty) =
        binding.ivIcon.let {
            SpringAnimation(binding.ivIcon, property, ICON_CLICK_SCALE).apply {
                addUpdateListener { _, _, _ ->
                    if (property.getValue(it) >= ICON_CLICK_SCALE) {
                        animateToFinalPosition(1f)
                    }
                }
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
            }
        }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupIconDragging() {
        val ivIcon = binding.ivIcon
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
                            v.parent.requestDisallowInterceptTouchEvent(true)
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
                            v.parent.requestDisallowInterceptTouchEvent(false)
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