package ru.luckycactus.steamroulette.presentation.features.detailed_description

import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import kotlinx.android.synthetic.main.fragment_detailed_description.*
import kotlinx.android.synthetic.main.fragment_detailed_description.toolbar
import kotlinx.android.synthetic.main.fragment_system_reqs.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.argument
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideImageGetter

class DetailedDescriptionFragment : BaseFragment() {
    private val appName: String by argument(ARG_APP_NAME)
    private val detailedDescription: String by argument(ARG_DETAILED_DESCRIPTION)

    override val layoutResId: Int = R.layout.fragment_detailed_description

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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