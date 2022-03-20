package ru.luckycactus.steamroulette.presentation.features.roulette

import android.graphics.Bitmap
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.GameCard
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.GameCardImageType

@Composable
fun RouletteGameCard(
    game: GameHeader,
    overlayHideAlpha: Float,
    modifier: Modifier = Modifier,
    onBitmapReady: (Bitmap) -> Unit
) {
    ConstraintLayout(modifier) {
        val (card, hide) = createRefs()

        GameCard(
            game = game,
            imageType = GameCardImageType.HD,
            defaultTextSize = 36.sp,
            onBitmapReady = onBitmapReady,
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(card) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        )

        Icon(
            painter = painterResource(R.drawable.ic_visibility_off_24dp),
            contentDescription = null,
            tint = colorResource(id = R.color.color_overlay_hide),
            modifier = Modifier
                .constrainAs(hide) {
                    width = Dimension.percent(0.25f)
                    height = Dimension.wrapContent
                    linkTo(
                        parent.start,
                        parent.top,
                        parent.end,
                        parent.bottom,
                        startMargin = Dimens.spacingNormal,
                        topMargin = Dimens.spacingNormal,
                        endMargin = Dimens.spacingNormal,
                        bottomMargin = Dimens.spacingNormal,
                        horizontalBias = 0.9f,
                        verticalBias = 0.05f
                    )
                }
                .aspectRatio(1f)
                .rotate(20f)
                .alpha(overlayHideAlpha)
        )
    }
}