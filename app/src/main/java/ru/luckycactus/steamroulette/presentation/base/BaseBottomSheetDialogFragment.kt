package ru.luckycactus.steamroulette.presentation.base

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.luckycactus.steamroulette.R

abstract class BaseBottomSheetDialogFragment: BottomSheetDialogFragment() {

    abstract val layoutResId: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutResId, container, false)
    }
}