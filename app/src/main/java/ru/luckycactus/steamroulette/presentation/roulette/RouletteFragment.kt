package ru.luckycactus.steamroulette.presentation.roulette

import android.content.res.Resources
import android.os.Bundle
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_roulette.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.lazyNonThreadSafe
import ru.luckycactus.steamroulette.presentation.observe

class RouletteFragment : BaseFragment() {

    private val viewModel by lazyNonThreadSafe {
        ViewModelProviders.of(this).get(RouletteViewModel::class.java)
    }

    private lateinit var behavior: BottomSheetBehavior<*>

    override val layoutResId: Int = R.layout.fragment_roulette

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        behavior = BottomSheetBehavior.from(view?.findViewById<ViewGroup>(R.id.menuSheet)!!)
        behavior.skipCollapsed = true
        behavior.isHideable = true
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        ivAvatar.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        observe(viewModel.userSummary) {
            tvNickname.text = it.personaName
        }
    }

    companion object {
        fun newInstance() = RouletteFragment()
    }
}