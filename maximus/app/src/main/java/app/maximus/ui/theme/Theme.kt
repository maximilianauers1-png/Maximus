package app.maximus.ui.theme

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.maximus.R

/**
 * "Eisen & Schwarz": blackened iron as ground, polished plate steel as the single accent,
 * blued steel (Brünierung) for secondary data and a muted heraldic red for Sundays, holidays and warnings.
 * Contrast ratios (WCAG 2.x, (L1 + 0.05)/(L2 + 0.05), sRGB relative luminance) against the ground:
 * linen 15.6:1, steel 10.4:1, muted 8.5:1, blued steel 7.7:1, heraldic red 7.0:1; dark text on steel 9.7:1.
 */
object Palette {
    val Ground = Color(0xFF0E0F11)
    val Lowest = Color(0xFF0A0B0C)
    val Low = Color(0xFF131417)
    val Container = Color(0xFF18191C)
    val High = Color(0xFF1F2124)
    val Highest = Color(0xFF272A2E)
    val Linen = Color(0xFFE6E8EA)
    val Muted = Color(0xFFA7ADB4)
    val Steel = Color(0xFFB8C0C8)
    val SteelDeep = Color(0xFF6E767F)
    val SteelLight = Color(0xFFE3E8EC)
    val Blued = Color(0xFF8FA7BF)
    val Heraldic = Color(0xFFD28C86)
    val Hairline = Color(0xFF2E3136)
}

private val Scheme = darkColorScheme(
    primary = Palette.Steel,
    onPrimary = Color(0xFF15181B),
    primaryContainer = Color(0xFF363B41),
    onPrimaryContainer = Palette.SteelLight,
    inversePrimary = Color(0xFF4E565E),
    secondary = Palette.Blued,
    onSecondary = Color(0xFF0F1A25),
    secondaryContainer = Color(0xFF223242),
    onSecondaryContainer = Color(0xFFCFE0F0),
    tertiary = Palette.Heraldic,
    onTertiary = Color(0xFF3A0F0C),
    tertiaryContainer = Color(0xFF4F1F1B),
    onTertiaryContainer = Color(0xFFF6D5D1),
    background = Palette.Ground,
    onBackground = Palette.Linen,
    surface = Palette.Ground,
    onSurface = Palette.Linen,
    surfaceVariant = Palette.Highest,
    onSurfaceVariant = Palette.Muted,
    surfaceTint = Palette.Steel,
    inverseSurface = Palette.Linen,
    inverseOnSurface = Palette.Ground,
    error = Color(0xFFE5877D),
    onError = Color(0xFF3B0A06),
    errorContainer = Color(0xFF5C1C17),
    onErrorContainer = Color(0xFFFFDAD5),
    outline = Color(0xFF5A6068),
    outlineVariant = Palette.Hairline,
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF32363B),
    surfaceDim = Palette.Ground,
    surfaceContainerLowest = Palette.Lowest,
    surfaceContainerLow = Palette.Low,
    surfaceContainer = Palette.Container,
    surfaceContainerHigh = Palette.High,
    surfaceContainerHighest = Palette.Highest
)

/** Grenze Gotisch: a blackletter-derived display face that stays legible at small sizes. */
val Gotisch = FontFamily(
    Font(R.font.grenze_medium, FontWeight.Medium),
    Font(R.font.grenze_semibold, FontWeight.SemiBold),
    Font(R.font.grenze_extrabold, FontWeight.ExtraBold)
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold)
)

/**
 * Two families with distinct roles: Grenze Gotisch (wordmark, headlines, titles) carries the
 * German gothic character; Inter carries all reading and data text, so tables stay practical.
 * Scale ratio ≈ 1.2 (minor third) from 16 sp body.
 */
private val Type = Typography(
    displayLarge = TextStyle(fontFamily = Gotisch, fontWeight = FontWeight.ExtraBold, fontSize = 58.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = Gotisch, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, lineHeight = 50.sp),
    displaySmall = TextStyle(fontFamily = Gotisch, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 42.sp),
    headlineLarge = TextStyle(fontFamily = Gotisch, fontWeight = FontWeight.SemiBold, fontSize = 31.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = Gotisch, fontWeight = FontWeight.SemiBold, fontSize = 27.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = Gotisch, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 29.sp),
    titleLarge = TextStyle(fontFamily = Gotisch, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
    labelMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
    labelSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp)
)

private val ShapeSet = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** MAXIMUS is dark by design; the parameters are kept for source compatibility and ignored. */
@Composable
fun MaximusTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true,
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = Scheme, typography = Type, shapes = ShapeSet, content = content)
}

enum class ModulePalette(val accent: Color, val accentSurface: Color, val ink: Color) {
    GENERAL(Color(0xFFE0C39B), Color(0xFF393024), Color(0xFF241B10)),
    NUTRITION(Color(0xFFB9D7A8), Color(0xFF2B3B2B), Color(0xFF162614)),
    STRONGMAN(Color(0xFFA9C9E9), Color(0xFF253747), Color(0xFF102337)),
    DND(Color(0xFFD4B7E6), Color(0xFF392C42), Color(0xFF2C1938))
}

@Composable
fun ModuleTheme(palette: ModulePalette, content: @Composable () -> Unit) {
    val nutrition = palette == ModulePalette.NUTRITION
    val ground = if (nutrition) Color(0xFF101612) else Color(0xFF111417)
    val scheme = Scheme.copy(
        primary = palette.accent,
        onPrimary = palette.ink,
        primaryContainer = palette.accentSurface,
        onPrimaryContainer = palette.accent,
        inversePrimary = palette.accentSurface,
        secondary = if (nutrition) Color(0xFFC9D3AD) else Color(0xFFB6C8D4),
        onSecondary = Color(0xFF1C281C),
        secondaryContainer = if (nutrition) Color(0xFF2C3726) else Color(0xFF29343C),
        onSecondaryContainer = if (nutrition) Color(0xFFD9E3C3) else Color(0xFFD8E6F0),
        tertiary = if (nutrition) Color(0xFFF2C58E) else Color(0xFFE4A99C),
        onTertiary = Color(0xFF352416),
        tertiaryContainer = Color(0xFF483223),
        onTertiaryContainer = Color(0xFFFFDFC2),
        background = ground,
        onBackground = Color(0xFFF0F1EA),
        surface = ground,
        onSurface = Color(0xFFF0F1EA),
        onSurfaceVariant = if (nutrition) Color(0xFFB5BFB2) else Color(0xFFBAC2C8),
        surfaceVariant = if (nutrition) Color(0xFF303A31) else Color(0xFF32393F),
        surfaceTint = palette.accent,
        inverseOnSurface = ground,
        outline = if (nutrition) Color(0xFF8A9988) else Color(0xFF89949F),
        outlineVariant = if (nutrition) Color(0xFF344135) else Color(0xFF333A42),
        surfaceDim = ground,
        surfaceBright = if (nutrition) Color(0xFF38453B) else Color(0xFF363E47),
        surfaceContainerLowest = if (nutrition) Color(0xFF0B100D) else Color(0xFF0C0E11),
        surfaceContainerLow = if (nutrition) Color(0xFF181F19) else Color(0xFF191D22),
        surfaceContainer = if (nutrition) Color(0xFF1E2720) else Color(0xFF1F242A),
        surfaceContainerHigh = if (nutrition) Color(0xFF263027) else Color(0xFF272D34),
        surfaceContainerHighest = if (nutrition) Color(0xFF303A31) else Color(0xFF30373F)
    )
    val typography = if (nutrition) Type.copy(
        displaySmall = Type.displaySmall.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        headlineMedium = Type.headlineMedium.copy(fontFamily = Inter),
        headlineSmall = Type.headlineSmall.copy(fontFamily = Inter),
        titleLarge = Type.titleLarge.copy(fontFamily = Inter)
    ) else Type
    MaterialTheme(colorScheme = scheme, typography = typography, shapes = ShapeSet, content = content)
}
