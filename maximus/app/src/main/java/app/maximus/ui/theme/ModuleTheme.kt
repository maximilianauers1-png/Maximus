package app.maximus.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.maximus.R

/** The app's modules; each can carry its own palette depending on the chosen [DesignConcept]. */
enum class ModuleStyle { DND, STRONGMAN, NUTRITION, LAB, MAXIMUS }

/** Ornament drawn under the top bar. */
enum class StripePattern { SLASHED, BANDS, METAL, SCAN }

/**
 * Colours of one module under one design concept. All accents keep ≥ 4.5:1 contrast to their ground.
 * [terminal] switches Maximus to monospace and the amber-phosphor knight.
 */
data class ModulePalette(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val tertiary: Color,
    val ground: Color,
    val low: Color,
    val container: Color,
    val high: Color,
    val highest: Color,
    val outline: Color,
    val hairline: Color,
    val stripe: List<Color>,
    val pattern: StripePattern,
    val terminal: Boolean = false
) {
    /** Gradient for titles and highlights. */
    val accentBrush: Brush get() = Brush.horizontalGradient(listOf(primary, secondary))
}

/** The palettes: deliberately few families instead of a rainbow. */
object Palettes {
    /** Dark medieval crimson and black with Landsknecht saffron and bone (D&D). */
    val LANDSKNECHT = ModulePalette(
        primary = Color(0xFFE0565C), onPrimary = Color(0xFF1A0607), secondary = Color(0xFFF2C14E), tertiary = Color(0xFFEDE3CF),
        ground = Color(0xFF0F0809), low = Color(0xFF180D0E), container = Color(0xFF211213), high = Color(0xFF2B1718), highest = Color(0xFF361D1F),
        outline = Color(0xFF7A3A3D), hairline = Color(0xFF3D2022),
        stripe = listOf(Color(0xFFB3202A), Color(0xFF0B0B0B), Color(0xFFF2C14E), Color(0xFFEDE3CF)), pattern = StripePattern.SLASHED
    )

    /** Black, red and gold: heraldic, calm, without the Landsknecht slashes. */
    val SCHWARZ_ROT_GOLD = ModulePalette(
        primary = Color(0xFFD9454D), onPrimary = Color(0xFF1A0607), secondary = Color(0xFFE8BE4F), tertiary = Color(0xFFEDE3CF),
        ground = Color(0xFF0C0A0A), low = Color(0xFF151112), container = Color(0xFF1C1617), high = Color(0xFF241C1D), highest = Color(0xFF2E2425),
        outline = Color(0xFF6E4A3A), hairline = Color(0xFF382726),
        stripe = listOf(Color(0xFF0B0B0B), Color(0xFFB3202A), Color(0xFFE8BE4F)), pattern = StripePattern.BANDS
    )

    /** Royal blue, white and silver on deep navy. */
    val BLAU_WEISS_SILBER = ModulePalette(
        primary = Color(0xFF7FAEFF), onPrimary = Color(0xFF06142B), secondary = Color(0xFFC9D1DB), tertiary = Color(0xFFF4F7FB),
        ground = Color(0xFF0A0D13), low = Color(0xFF0F141C), container = Color(0xFF141B26), high = Color(0xFF1A2331), highest = Color(0xFF222D3D),
        outline = Color(0xFF4A5D7A), hairline = Color(0xFF23304A),
        stripe = listOf(Color(0xFF2F6FE0), Color(0xFFF4F7FB), Color(0xFFC9D1DB), Color(0xFF2F6FE0)), pattern = StripePattern.METAL
    )

    /** Retro terminal for Maximus: amber and green phosphor on black, monospace. */
    val TERMINAL = ModulePalette(
        primary = Color(0xFFFFB000), onPrimary = Color(0xFF1A1000), secondary = Color(0xFF41FF7A), tertiary = Color(0xFFFF6B5B),
        ground = Color(0xFF050505), low = Color(0xFF0B0B09), container = Color(0xFF12110D), high = Color(0xFF1A1812), highest = Color(0xFF242117),
        outline = Color(0xFF6B5A2A), hairline = Color(0xFF2B2616),
        stripe = listOf(Color(0xFFFFB000), Color(0xFF41FF7A)), pattern = StripePattern.SCAN, terminal = true
    )
}

/**
 * Switchable design concepts. D&D keeps its red and black in every concept except the classic one,
 * Maximus keeps the terminal; "Klassisch" is the original steel design without module colours.
 */
enum class DesignConcept(val label: String, val description: String) {
    RITTERORDEN("Ritterorden", "Schwarz-Rot-Gold für D&D und Strongman, Blau-Weiß-Silber für Labor und Ernährung, Maximus als Terminal"),
    SCHWARZ_ROT_GOLD("Schwarz-Rot-Gold", "Alle Bereiche in Schwarz, Rot und Gold; D&D mit Landsknecht-Muster, Maximus als Terminal"),
    BLAU_WEISS_SILBER("Blau-Weiß-Silber", "Alle Bereiche in Königsblau, Weiß und Silber; D&D bleibt rot-schwarz, Maximus als Terminal"),
    KLASSISCH("Klassisch", "Das ursprüngliche Design: dunkles Eisen und poliertes Silber, überall gleich");

    fun palette(module: ModuleStyle): ModulePalette? = when (this) {
        KLASSISCH -> null
        RITTERORDEN -> when (module) {
            ModuleStyle.DND -> Palettes.LANDSKNECHT
            ModuleStyle.STRONGMAN -> Palettes.SCHWARZ_ROT_GOLD
            ModuleStyle.NUTRITION, ModuleStyle.LAB -> Palettes.BLAU_WEISS_SILBER
            ModuleStyle.MAXIMUS -> Palettes.TERMINAL
        }
        SCHWARZ_ROT_GOLD -> when (module) {
            ModuleStyle.DND -> Palettes.LANDSKNECHT
            ModuleStyle.MAXIMUS -> Palettes.TERMINAL
            else -> Palettes.SCHWARZ_ROT_GOLD
        }
        BLAU_WEISS_SILBER -> when (module) {
            ModuleStyle.DND -> Palettes.LANDSKNECHT
            ModuleStyle.MAXIMUS -> Palettes.TERMINAL
            else -> Palettes.BLAU_WEISS_SILBER
        }
    }

    /** Restrained tones for the lab topics (null = the original per-topic colours). */
    val topicTones: List<Color>?
        get() = when (this) {
            KLASSISCH -> null
            SCHWARZ_ROT_GOLD -> listOf(Color(0xFFE0565C), Color(0xFFE8BE4F), Color(0xFFEDE3CF), Color(0xFFF2D98A))
            RITTERORDEN, BLAU_WEISS_SILBER -> listOf(Color(0xFF7FAEFF), Color(0xFFC9D1DB), Color(0xFFF4F7FB), Color(0xFFA9C4EE))
        }

    /** Swatches for the picker. */
    val swatches: List<Color>
        get() = when (this) {
            RITTERORDEN -> listOf(Color(0xFF0B0B0B), Color(0xFFD9454D), Color(0xFFE8BE4F), Color(0xFF7FAEFF), Color(0xFFC9D1DB))
            SCHWARZ_ROT_GOLD -> listOf(Color(0xFF0B0B0B), Color(0xFFD9454D), Color(0xFFE8BE4F))
            BLAU_WEISS_SILBER -> listOf(Color(0xFF2F6FE0), Color(0xFFF4F7FB), Color(0xFFC9D1DB))
            KLASSISCH -> listOf(Palette.Ground, Palette.Steel, Palette.SteelLight)
        }
}

/**
 * The active concept as snapshot state: reading it in composition recomposes everything that depends
 * on it when the user switches designs. Persisted by the settings (DesignRepository).
 */
object Design {
    var concept by mutableStateOf(DesignConcept.RITTERORDEN)
}

val LocalModuleStyle = staticCompositionLocalOf<ModulePalette?> { null }

private fun Typography.monospaced(): Typography {
    fun TextStyle.mono() = copy(fontFamily = FontFamily.Monospace)
    return copy(
        displayLarge = displayLarge.mono(), displayMedium = displayMedium.mono(), displaySmall = displaySmall.mono(),
        headlineLarge = headlineLarge.mono(), headlineMedium = headlineMedium.mono(), headlineSmall = headlineSmall.mono(),
        titleLarge = titleLarge.mono(), titleMedium = titleMedium.mono(), titleSmall = titleSmall.mono(),
        bodyLarge = bodyLarge.mono().copy(fontSize = bodyLarge.fontSize * 0.94f), bodyMedium = bodyMedium.mono(), bodySmall = bodySmall.mono(),
        labelLarge = labelLarge.mono(), labelMedium = labelMedium.mono(), labelSmall = labelSmall.mono()
    )
}

/** Applies the module's palette of the active design concept (and, for Maximus, the terminal typography). */
@Composable
fun ModuleTheme(style: ModuleStyle, content: @Composable () -> Unit) {
    val palette = Design.concept.palette(style)
    if (palette == null) {
        CompositionLocalProvider(LocalModuleStyle provides null, content = content)
        return
    }
    val base = MaterialTheme.colorScheme
    val baseType = MaterialTheme.typography
    val scheme = remember(palette, base) {
        base.copy(
            primary = palette.primary, onPrimary = palette.onPrimary,
            primaryContainer = palette.primary.copy(alpha = 0.22f).compositeOver(palette.container), onPrimaryContainer = palette.primary,
            secondary = palette.secondary, secondaryContainer = palette.secondary.copy(alpha = 0.18f).compositeOver(palette.container), onSecondaryContainer = palette.secondary,
            tertiary = palette.tertiary, tertiaryContainer = palette.tertiary.copy(alpha = 0.18f).compositeOver(palette.container), onTertiaryContainer = palette.tertiary,
            background = palette.ground, surface = palette.ground, surfaceDim = palette.ground,
            surfaceContainerLowest = palette.ground, surfaceContainerLow = palette.low, surfaceContainer = palette.container,
            surfaceContainerHigh = palette.high, surfaceContainerHighest = palette.highest, surfaceVariant = palette.highest,
            surfaceTint = palette.primary, outline = palette.outline, outlineVariant = palette.hairline
        )
    }
    val type = remember(palette, baseType) { if (palette.terminal) baseType.monospaced() else baseType }
    CompositionLocalProvider(LocalModuleStyle provides palette) {
        MaterialTheme(colorScheme = scheme, typography = type, shapes = MaterialTheme.shapes, content = content)
    }
}

private fun Color.compositeOver(bg: Color): Color {
    val a = alpha
    return Color(red * a + bg.red * (1 - a), green * a + bg.green * (1 - a), blue * a + bg.blue * (1 - a), 1f)
}

/**
 * Decorative band under the top bar.
 *  • SLASHED (D&D): Landsknecht doublet — diagonal crimson/black panels with saffron and bone slashes.
 *  • BANDS: three heraldic bands (black, red, gold).
 *  • METAL: brushed blue-white-silver gradient with fine engraving ticks.
 *  • SCAN: phosphor scan bar (Maximus).
 */
@Composable
fun ModuleStripe(palette: ModulePalette, modifier: Modifier = Modifier) {
    val h = when (palette.pattern) { StripePattern.SLASHED -> 9.dp; StripePattern.BANDS -> 6.dp; else -> 4.dp }
    Canvas(modifier.fillMaxWidth().height(h)) {
        when (palette.pattern) {
            StripePattern.SLASHED -> {
                val red = palette.stripe[0]; val black = palette.stripe[1]; val gold = palette.stripe[2]; val bone = palette.stripe[3]
                val w = size.height * 2.2f
                clipRect {
                    var x = -size.height
                    var i = 0
                    while (x < size.width + size.height) {
                        val p = Path().apply {
                            moveTo(x, size.height); lineTo(x + size.height, 0f); lineTo(x + size.height + w, 0f); lineTo(x + w, size.height); close()
                        }
                        drawPath(p, if (i % 2 == 0) red else black)
                        val cx = x + w / 2 + size.height / 2
                        drawLine(if (i % 4 == 0) gold else bone, Offset(cx - 1.5f, size.height * 0.25f), Offset(cx + 1.5f, size.height * 0.75f), strokeWidth = size.height * 0.22f)
                        x += w
                        i++
                    }
                }
                drawLine(gold, Offset(0f, size.height - 0.5f), Offset(size.width, size.height - 0.5f), strokeWidth = 1f)
            }
            StripePattern.BANDS -> {
                val band = size.height / palette.stripe.size
                palette.stripe.forEachIndexed { i, c -> drawRect(c, Offset(0f, i * band), androidx.compose.ui.geometry.Size(size.width, band)) }
            }
            StripePattern.METAL -> {
                drawRect(Brush.horizontalGradient(palette.stripe))
                var x = 0f
                while (x < size.width) { drawLine(palette.ground.copy(alpha = 0.35f), Offset(x, 0f), Offset(x, size.height), 1f); x += 28f }
            }
            StripePattern.SCAN -> {
                drawRect(Brush.horizontalGradient(listOf(palette.ground, palette.primary, palette.secondary, palette.ground)))
                var x = 0f
                while (x < size.width) { drawRect(palette.ground, Offset(x, 0f), androidx.compose.ui.geometry.Size(2f, size.height)); x += 6f }
            }
        }
    }
}

/** Luminance → amber phosphor (retro terminal look) for the knight avatar. */
private val AmberPhosphor = ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
    0.30f, 0.59f, 0.11f, 0f, 20f,
    0.20f, 0.40f, 0.07f, 0f, 8f,
    0.02f, 0.05f, 0.01f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f
)))

/** Maximus's face: the knight from the home screen, optionally in amber phosphor. */
@Composable
fun KnightAvatar(size: Dp, modifier: Modifier = Modifier, phosphor: Boolean = LocalModuleStyle.current?.terminal ?: false, border: Color? = null) {
    val shape = RoundedCornerShape(size * 0.22f)
    Image(
        painter = painterResource(R.drawable.knight_avatar),
        contentDescription = "Maximus",
        contentScale = ContentScale.Crop,
        colorFilter = if (phosphor) AmberPhosphor else null,
        modifier = modifier.size(size).clip(shape).let { if (border != null) it.border(1.dp, border, shape) else it }
    )
}
