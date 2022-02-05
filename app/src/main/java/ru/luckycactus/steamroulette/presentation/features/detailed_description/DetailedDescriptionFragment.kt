package ru.luckycactus.steamroulette.presentation.features.detailed_description

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.FragmentDetailedDescriptionBinding
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.addSystemTopPadding
import ru.luckycactus.steamroulette.presentation.utils.extensions.argument
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideImageGetter

class DetailedDescriptionFragment : BaseFragment(R.layout.fragment_detailed_description) {

    private val binding by viewBinding(FragmentDetailedDescriptionBinding::bind)

    private var appName: String by argument()
    private var detailedDescription: String by argument()

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
        fun newInstance(appName: String, detailedDescription: String) =
            DetailedDescriptionFragment().apply {
                this.appName = appName
                this.detailedDescription = detailedDescription
            }
    }
}