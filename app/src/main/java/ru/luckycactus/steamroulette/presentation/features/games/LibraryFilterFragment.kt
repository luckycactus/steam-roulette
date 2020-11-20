package ru.luckycactus.steamroulette.presentation.features.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import dagger.hilt.android.AndroidEntryPoint
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.FragmentLibraryFilterBinding
import ru.luckycactus.steamroulette.domain.common.Consts
import ru.luckycactus.steamroulette.presentation.features.roulette_options.RouletteFiltersDialog
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.AdvancedBottomSheetCallback
import ru.luckycactus.steamroulette.presentation.utils.bsOffsetToAlpha
import ru.luckycactus.steamroulette.presentation.utils.extensions.changeThroughFade
import ru.luckycactus.steamroulette.presentation.utils.extensions.hideKeyboard
import ru.luckycactus.steamroulette.presentation.utils.extensions.observe
import ru.luckycactus.steamroulette.presentation.utils.extensions.visibility


@AndroidEntryPoint
class LibraryFilterFragment : BaseFragment<FragmentLibraryFilterBinding>() {

    private val viewModel: LibraryViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private lateinit var behavior: BottomSheetBehavior<*>

    override val logScreenName: Nothing? = null

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentLibraryFilterBinding.inflate(inflater, container, false)

    // it should be onActivityCreated and not onViewCreated, because we need behaviour, but
    // in onViewCreated fragment isn't added into hierarchy yet
    override fun onActivityCreated(savedInstanceState: Bundle?) : Unit = with(binding) {
        super.onActivityCreated(savedInstanceState)

        val shapeAppearanceModel = ShapeAppearanceModel.builder(
            requireContext(),
            R.style.ShapeAppearance_App_BottomSheet,
            0
        ).build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        filterSheet.background = materialShapeDrawable

        behavior = from(filterSheet)
        val bottomSheetCallback = object : AdvancedBottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int, previousState: Int) {
                if (previousState == STATE_EXPANDED) {
                    viewModel.onFilterSheetClosingStarted()
                }
                if (newState == STATE_COLLAPSED || newState == STATE_HIDDEN) {
                    activity?.hideKeyboard(etPlaytime)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float, diff: Float, state: Int) {
                updateHeadersAlpha(slideOffset)
            }
        }
        behavior.addBottomSheetCallback(bottomSheetCallback)

        filterSheet.doOnLayout {
            bottomSheetCallback.setState(behavior.state)
            val slideOffset = when (behavior.state) {
                STATE_EXPANDED -> 1f
                STATE_COLLAPSED -> 0f
                else -> -1f
            }
            updateHeadersAlpha(slideOffset)
        }

        if (viewModel.onlyHidden) {
            expand.setOnClickListener { /* nothing */ }
        } else {
            expand.setOnClickListener {
                behavior.state = STATE_EXPANDED
            }
        }

        btnCollapse.setOnClickListener {
            close()
        }

        btnResetShortcut.setOnClickListener {
            reset()
        }

        btnReset.setOnClickListener {
            reset()
        }

        rgLibraryFilter.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.rbAll -> LibraryViewModel.LibraryFilter.All
                R.id.rbHidden -> LibraryViewModel.LibraryFilter.Hidden
                R.id.rbNotPlayed -> LibraryViewModel.LibraryFilter.NotPlayed
                R.id.rbLimit -> LibraryViewModel.LibraryFilter.Limited
                else -> throw IllegalStateException()
            }
            viewModel.onFilterSelectionChanged(filter)
        }

        rbLimit.setOnCheckedChangeListener { _, isChecked ->
            etPlaytime.isEnabled = isChecked
        }
        etPlaytime.filters += RouletteFiltersDialog.MinMaxInputFilter(
            Consts.FILTER_MIN_HOURS,
            Consts.FILTER_MAX_HOURS
        )
        etPlaytime.label = getString(R.string.playtime_hours_label)

        etPlaytime.addTextChangedListener {
            val maxHours = try {
                Integer.parseInt(etPlaytime.textWithoutLabel.toString())
            } catch (nfe: NumberFormatException) {
                1
            }
            viewModel.onMaxHoursChanged(maxHours)
        }

        observe(viewModel.libraryFilter) {
            when (it) {
                LibraryViewModel.LibraryFilter.All -> rbAll
                LibraryViewModel.LibraryFilter.Hidden -> rbHidden
                LibraryViewModel.LibraryFilter.NotPlayed -> rbNotPlayed
                LibraryViewModel.LibraryFilter.Limited -> rbLimit
            }.isChecked = true
        }

        observe(viewModel.filteredGamesCount) {
            val newText = resources.getQuantityString(R.plurals.games_count_plurals, it, it)
            if (tvTitle.text != newText) {
                tvTitle.changeThroughFade {
                    tvTitle.text = newText
                }
            }
        }

        observe(viewModel.maxHours) {
            etPlaytime.setText(it.toString())
        }

        observe(viewModel.selectedFilterText) {
            chipSelectedFilter.text = it
            chipSelectedFilter.visibility(!it.isNullOrEmpty())
        }

        observe(viewModel.hasAnyFilters) {
            btnResetShortcut.visibility(it && !viewModel.onlyHidden)

            btnReset.isClickable = it
            val targetAlpha = if (it) 1f else 0f
            if (btnReset.alpha != targetAlpha) {
                btnReset.animate().alpha(targetAlpha).setDuration(200)
            }
        }
    }

    fun open(open: Boolean = true) {
        behavior.state = when {
            open -> STATE_EXPANDED
            behavior.skipCollapsed && behavior.isHideable -> STATE_HIDDEN
            else -> STATE_COLLAPSED
        }
    }

    fun close() {
        open(false)
    }

    private fun reset() {
        viewModel.clearFilters()
        close()
    }

    private fun updateHeadersAlpha(slideOffset: Float): Unit = with(binding) {
        expanded.alpha = bsOffsetToAlpha(slideOffset, 0.36f, 0.67f)
        expanded.visibility = if (expanded.alpha > 0f) View.VISIBLE else View.INVISIBLE

        collapsed.alpha = bsOffsetToAlpha(slideOffset, 0.36f, 0f)
        collapsed.visibility = if (collapsed.alpha > 0f) View.VISIBLE else View.INVISIBLE
    }
}
