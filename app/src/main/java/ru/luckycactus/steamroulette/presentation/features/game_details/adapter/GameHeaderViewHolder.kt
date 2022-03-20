package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.graphics.Bitmap
import androidx.compose.ui.unit.sp
import ru.luckycactus.steamroulette.databinding.ItemGameDetailsHeaderBinding
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.GameCardImageType
import ru.luckycactus.steamroulette.presentation.utils.extensions.visibility

class GameHeaderViewHolder(
    private val binding: ItemGameDetailsHeaderBinding,
    onBitmapReady: (Bitmap) -> Unit
) : GameDetailsViewHolder<GameDetailsUiModel.Header>(binding.root) {

    init {
        with(binding.gameView) {
            memoryCacheEnabled = true
            this.onBitmapReady = onBitmapReady
            defaultTextSize = 16.sp
            imageType = GameCardImageType.HdIfCachedOrSd
        }
    }

    override fun bind(
        item: GameDetailsUiModel.Header
    ): Unit = with(binding) {
        tvHeaderGameName.text = item.gameHeader.name
        tvPublisher.text = item.publisher
        tvPublisher.visibility(!item.publisher.isNullOrBlank())
        tvDeveloper.text = item.developer
        tvDeveloper.visibility(!item.developer.isNullOrBlank())
        tvReleaseDate.text = item.releaseDate
        tvReleaseDate.visibility(!item.releaseDate.isNullOrBlank())
        gameView.game = item.gameHeader
    }
}