package ru.luckycactus.steamroulette.presentation.roulette.options

import android.app.Dialog
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_pref_playtime.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.findComponent
import ru.luckycactus.steamroulette.domain.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.main.MainFlowComponent
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.observeFirst
import ru.luckycactus.steamroulette.presentation.utils.viewModel


class PlaytimePrefDialog : DialogFragment() {

    private var layoutContainer: DialogLayoutContainer? = null

    private val viewModel by viewModel {
        findComponent<MainFlowComponent>().playtimeDialogViewModel
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return with(MaterialAlertDialogBuilder(context!!)) {
            setTitle(getString(R.string.playtime))

            with(LayoutInflater.from(context).inflate(R.layout.dialog_pref_playtime, null)) {
                setView(this)
                layoutContainer = DialogLayoutContainer(this, savedInstanceState)
            }

            setNegativeButton(R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ ->
                with(layoutContainer!!) {
                    val newFilterType = when {
                        rbPlaytimeAll.isChecked -> PlaytimeFilter.Type.All
                        rbPlayTimeNotPlayed.isChecked -> PlaytimeFilter.Type.NotPlayed
                        rbPlaytimeLimit.isChecked -> PlaytimeFilter.Type.Limited
                        else -> throw IllegalStateException()
                    }
                    val newMaxPlaytime = try {
                        Integer.parseInt(etPlaytime.textWithoutLabel.toString())
                    } catch (nfe: NumberFormatException) {
                        1
                    }
                    viewModel.onOkClick(newFilterType, newMaxPlaytime)
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
            rbPlaytimeLimit.setOnCheckedChangeListener { _, isChecked ->
                etPlaytime.isEnabled = isChecked
            }

            etPlaytime.filters = arrayOf(MinMaxInputFilter(1, 1000))
            etPlaytime.label = getString(R.string.playtime_hours_label)

            if (savedInstanceState == null) {
                observeFirst(viewModel.currentPlaytimeFilterType) {
                    when (it) {
                        PlaytimeFilter.Type.All -> rbPlaytimeAll
                        PlaytimeFilter.Type.NotPlayed -> rbPlayTimeNotPlayed
                        PlaytimeFilter.Type.Limited -> rbPlaytimeLimit
                    }.isChecked = true
                }

                observeFirst(viewModel.currentMaxPlaytimeSetting) {
                    etPlaytime.setText(it.toString())
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
        fun newInstance() = PlaytimePrefDialog()
    }

}