package ru.luckycactus.steamroulette.presentation.features.roulette_options

import android.app.Dialog
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_roulette_filters.*
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.Consts
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

@AndroidEntryPoint
class RouletteFiltersDialog : DialogFragment() {

    private var layoutContainer: DialogLayoutContainer? = null

    private val viewModel: RouletteFiltersViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return with(MaterialAlertDialogBuilder(requireContext())) {
            setTitle(getString(R.string.playtime))

            with(LayoutInflater.from(context).inflate(R.layout.dialog_roulette_filters, null)) {
                setView(this)
                layoutContainer = DialogLayoutContainer(this, savedInstanceState)
            }

            setNegativeButton(R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ ->
                with(layoutContainer!!) {
                    val filter = when {
                        rbAll.isChecked -> PlaytimeFilter.All
                        rbNotPlayed.isChecked -> PlaytimeFilter.NotPlayed
                        rbLimit.isChecked -> {
                            val maxHours = try {
                                Integer.parseInt(etPlaytime.textWithoutLabel.toString())
                            } catch (nfe: NumberFormatException) {
                                1
                            }
                            PlaytimeFilter.Limited(maxHours)
                        }
                        else -> throw IllegalStateException()
                    }

                    viewModel.onOkClick(filter)
                }
            }

            create()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        layoutContainer = null
    }

    //workaround to force the compiler to cache views
    inner class DialogLayoutContainer(
        override val containerView: View?,
        savedInstanceState: Bundle?
    ) : LayoutContainer {
        init {
            rbLimit.setOnCheckedChangeListener { _, isChecked ->
                etPlaytime.isEnabled = isChecked
            }

            etPlaytime.filters += MinMaxInputFilter(
                Consts.FILTER_MIN_HOURS,
                Consts.FILTER_MAX_HOURS
            )
            etPlaytime.label = getString(R.string.playtime_hours_label)

            if (savedInstanceState == null) {
                lifecycleScope.launch {
                    when (viewModel.getCurrentPlaytimeFilter()) {
                        PlaytimeFilter.All -> rbAll
                        PlaytimeFilter.NotPlayed -> rbNotPlayed
                        is PlaytimeFilter.Limited -> rbLimit
                    }.isChecked = true
                }

                lifecycleScope.launch {
                    etPlaytime.setText(viewModel.getCurrentMaxPlaytimeSetting().toString())
                }
            }
        }
    }

    class MinMaxInputFilter(
        private val min: Int,
        private val max: Int
    ) : InputFilter {
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            try {
                val new = TextUtils.concat(
                    dest.subSequence(0, dstart),
                    source,
                    dest.subSequence(dend, dest.length)
                )
                val firstPart = new.split(' ')[0]
                if (firstPart.isEmpty() || Integer.parseInt(firstPart) in min..max) {
                    return null
                }
            } catch (ignored: NumberFormatException) {
            }
            return ""
        }

    }

    companion object {
        fun newInstance() = RouletteFiltersDialog()
    }

}