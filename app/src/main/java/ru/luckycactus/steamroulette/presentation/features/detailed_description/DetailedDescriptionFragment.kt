package ru.luckycactus.steamroulette.presentation.features.detailed_description

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import ru.luckycactus.steamroulette.databinding.FragmentDetailedDescriptionBinding
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.addSystemTopPadding
import ru.luckycactus.steamroulette.presentation.utils.extensions.argument
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideImageGetter

class DetailedDescriptionFragment : BaseFragment<FragmentDetailedDescriptionBinding>() {

    private val appName: String by argument(ARG_APP_NAME)
    private val detailedDescription: String by argument(ARG_DETAILED_DESCRIPTION)

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentDetailedDescriptionBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.addSystemTopPadding()

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        toolbar.title = appName
        tvDescription.text = HtmlCompat.fromHtml(
            detailedDescription,
            0,
            GlideImageGetter(tvDescription, true, null),
            null
        )
        tvDescription.movementMethod = LinkMovementMethod.getInstance();
    }

    companion object {
        private const val ARG_APP_NAME = "ARG_GAME_ID"
        private const val ARG_DETAILED_DESCRIPTION = "ARG_DETAILED_DESCRIPTION"

        fun newInstance(appName: String, detailedDescription: String) =
            DetailedDescriptionFragment().apply {
                arguments = bundleOf(
                    ARG_APP_NAME to appName,
                    ARG_DETAILED_DESCRIPTION to detailedDescription
                )
            }
    }
}