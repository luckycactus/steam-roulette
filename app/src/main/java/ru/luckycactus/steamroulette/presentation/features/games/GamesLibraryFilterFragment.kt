package ru.luckycactus.steamroulette.presentation.features.games

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_library_filter.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.observe


@AndroidEntryPoint
class GamesLibraryFilterFragment : BaseFragment() {

    private val viewModel: GamesLibraryViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private lateinit var behavior: BottomSheetBehavior<*>

    private lateinit var expandedViews: List<View>
    private lateinit var collapsedViews: List<View>

    override val layoutResId: Int = R.layout.fragment_library_filter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        expandedViews = listOf(tvTitle, rgLibraryFilter, etPlaytime, btnCollapse, btnReset)
        collapsedViews = listOf(viewSelectedFilters, btnResetShortcut)

        val shapeAppearanceModel = ShapeAppearanceModel.builder(
            requireContext(),
            R.style.ShapeAppearance_MyTheme_BottomSheet,
            0
        ).build()

        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        filterSheet.background = materialShapeDrawable

        behavior = BottomSheetBehavior.from(filterSheet)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val expandedAlpha = slideOffset.coerceAtLeast(0f)
                //todo viewbinding
                expandedViews.forEach {
                    it.alpha = expandedAlpha
                }
                collapsedViews.forEach {
                    it.alpha = 1f - expandedAlpha

                }
            }

        })

        expand.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        btnResetShortcut.setOnClickListener {
            //todo library
        }

        btnReset.setOnClickListener {
            //todo library
        }

        rgLibraryFilter.setOnCheckedChangeListener { _, checkedId ->
            val filter = when(checkedId) {
                R.id.rbAll -> GamesLibraryViewModel.FilterType.All
                R.id.rbHidden -> GamesLibraryViewModel.FilterType.Hidden
                R.id.rbNotPlayed -> GamesLibraryViewModel.FilterType.NotPlayed
                R.id.rbLimit -> GamesLibraryViewModel.FilterType.Limited
                else -> throw IllegalStateException()
            }
            viewModel.onFilterSelectionChanged(filter)
        }

        observe(viewModel.currentFilter) {
            Log.d("ololo", it.toString())
            when (it) {
                GamesLibraryViewModel.FilterType.All -> rbAll
                GamesLibraryViewModel.FilterType.Hidden -> rbHidden
                GamesLibraryViewModel.FilterType.NotPlayed -> rbNotPlayed
                GamesLibraryViewModel.FilterType.Limited -> rbLimit
            }.isChecked = true
        }

        observe(viewModel.filteredGamesCount) {
            //todo library
            tvTitle.text = "$it games"
        }
    }
}