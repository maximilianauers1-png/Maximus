package app.maximus.ui.theme

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.maximus.R

enum class BrandArtwork(@DrawableRes val hero: Int, @DrawableRes val avatar: Int, @StringRes val label: Int) {
    ATHLETE(R.drawable.athlete_hero, R.drawable.athlete_avatar, R.string.design_artwork_athlete),
    KNIGHT(R.drawable.knight_hero, R.drawable.knight_avatar, R.string.design_artwork_knight)
}

object Design {
    var artwork by mutableStateOf(BrandArtwork.ATHLETE)
}

@Composable
fun MaximusAvatar(size: Dp, modifier: Modifier = Modifier, border: Color? = null) {
    Image(
        painter = painterResource(Design.artwork.avatar),
        contentDescription = null,
        modifier = modifier.size(size).clip(CircleShape)
            .then(if (border == null) Modifier else Modifier.border(1.dp, border, CircleShape)),
        contentScale = ContentScale.Crop
    )
}
