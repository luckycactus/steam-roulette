package ru.luckycactus.steamroulette.presentation.features.imageview

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import dagger.hilt.android.AndroidEntryPoint
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.utils.extensions.argument

@AndroidEntryPoint
class ImageGalleryPagerFragment: BaseFragment() {

    private var items: List<Parcelable> by argument()
    private var index: Int by argument()
    private var url: (Parcelable) -> String by argument()
    private var thumbnail: (Parcelable) -> String? by argument()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SteamRouletteTheme {
                    ImageGalleryPager(items, index, url, thumbnail)
                }
            }
        }
    }

    companion object {
        fun <T: Parcelable> newInstance(
            items: List<T>,
            index: Int,
            url: (T) -> String,
            thumbnail: (T) -> String?
        ) = ImageGalleryPagerFragment().apply {
            this.items = items
            this.index = index
            this.url = url as (Parcelable) -> String
            this.thumbnail = thumbnail as (Parcelable) -> String?
        }
    }
}