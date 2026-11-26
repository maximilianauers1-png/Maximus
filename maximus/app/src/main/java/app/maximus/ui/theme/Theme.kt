package app.maximus.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.maximus.R

object Palette {
    val Ground = Color(0xFF111010)
    val Lowest = Color(0xFF0D0C0C)
    val Low = Color(0xFF191717)
    val Container = Color(0xFF1E1B1B)
    val High = Color(0xFF262222)
    val Highest = Color(0xFF2E2928)
    val Linen = Color(0xFFF2EDE7)
    val Muted = Color(0xFFBFB5AD)
    val Copper = Color(0xFFE0B18B)
    val Slate = Color(0xFFB1BCC4)
    val Warning = Color(0xFFF1A69C)
    val Hairline = Color(0xFF423733)
}

private val Scheme = darkColorScheme(
    primary = Palette.Copper,
    onPrimary = Color(0xFF27170F),
    primaryContainer = Color(0xFF422D23),
    onPrimaryContainer = Color(0xFFF6D5BB),
    inversePrimary = Color(0xFF785136),
    secondary = Palette.Slate,
    onSecondary = Color(0xFF1C272C),
    secondaryContainer = Color(0xFF2C353A),
    onSecondaryContainer = Color(0xFFDFE6EA),
    tertiary = Color(0xFFC1CAA9),
    onTertiary = Color(0xFF252C1C),
    tertiaryContainer = Color(0xFF333A29),
    onTertiaryContainer = Color(0xFFDFE8CB),
    background = Palette.Ground,
    onBackground = Palette.Linen,
    surface = Palette.Ground,
    onSurface = Palette.Linen,
    surfaceVariant = Palette.Highest,
    onSurfaceVariant = Palette.Muted,
    surfaceTint = Palette.Copper,
    inverseSurface = Palette.Linen,
    inverseOnSurface = Palette.Ground,
    error = Palette.Warning,
    onError = Color(0xFF421913),
    errorContainer = Color(0xFF532623),
    onErrorContainer = Color(0xFFFFDAD4),
    outline = Color(0xFF9D8F85),
    outlineVariant = Palette.Hairline,
    scrim = Color.Black,
    surfaceBright = Color(0xFF3A322F),
    surfaceDim = Palette.Ground,
    surfaceContainerLowest = Palette.Lowest,
    surfaceContainerLow = Palette.Low,
    surfaceContainer = Palette.Container,
    surfaceContainerHigh = Palette.High,
    surfaceContainerHighest = Palette.Highest
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold)
)

private val Type = Typography(
    displayLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 54.sp, lineHeight = 60.sp, letterSpacing = (-1.5).sp),
    displayMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 44.sp, lineHeight = 50.sp, letterSpacing = (-1).sp),
    displaySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.7).sp),
    headlineLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp),
    labelSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp)
)

private val ShapeSet = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MaximusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = Type, shapes = ShapeSet, content = content)
}
