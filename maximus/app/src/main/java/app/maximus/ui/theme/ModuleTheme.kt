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
import androidx.compose.runtime.remember
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

/**
 * Per-module colour worlds on top of the dark iron base theme. Every module keeps the dark ground and
 * the typography, but gets its own accent family, tinted surfaces and a decorative stripe under the top
 * bar. All accents are chosen for ≥ 4.5:1 contrast against their ground.
 *
 *  • D&D: dark medieval crimson and black with Landsknecht accents (saffron, bone white, slashes).
 *  • Strongman: forge — molten orange on charcoal, blued steel.
 *  • Nutrition: herb green, saffron and tomato.
 *  • Lab: plasma — electric cyan, violet and magenta on deep navy.
 *  • Maximus: retro terminal — phosphor amber and green on black, monospace.
 */
enum class ModuleStyle(
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
    val stripe: List<Color>
) {
    DND(
        primary = Color(0xFFE0565C), onPrimary = Color(0xFF1A0607), secondary = Color(0xFFF2C14E), tertiary = Color(0xFFEDE3CF),
        ground = Color(0xFF0F0809), low = Color(0xFF180D0E), container = Color(0xFF211213), high = Color(0xFF2B1718), highest = Color(0xFF361D1F),
        outline = Color(0xFF7A3A3D), hairline = Color(0xFF3D2022),
        stripe = listOf(Color(0xFFB3202A), Color(0xFF0B0B0B), Color(0xFFF2C14E), Color(0xFFEDE3CF))
    ),
    STRONGMAN(
        primary = Color(0xFFFF8A3D), onPrimary = Color(0xFF241004), secondary = Color(0xFF8FB3D9), tertiary = Color(0xFFFFC857),
        ground = Color(0xFF0F0D0C), low = Color(0xFF171412), container = Color(0xFF1E1A17), high = Color(0xFF26211D), highest = Color(0xFF302924),
        outline = Color(0xFF6F5A4C), hairline = Color(0xFF352C26),
        stripe = listOf(Color(0xFF7A1E05), Color(0xFFFF5A1F), Color(0xFFFFB347), Color(0xFFFFE6A8))
    ),
    NUTRITION(
        primary = Color(0xFF79D98A), onPrimary = Color(0xFF07210E), secondary = Color(0xFFF2B544), tertiary = Color(0xFFFF8A7A),
        ground = Color(0xFF0C100D), low = Color(0xFF121813), container = Color(0xFF172019), high = Color(0xFF1E2920), highest = Color(0xFF263328),
        outline = Color(0xFF557A5C), hairline = Color(0xFF26352A),
        stripe = listOf(Color(0xFF2E7D32), Color(0xFF79D98A), Color(0xFFF2B544), Color(0xFFFF8A7A))
    ),
    LAB(
        primary = Color(0xFF5FD8EE), onPrimary = Color(0xFF031B21), secondary = Color(0xFFA899FF), tertiary = Color(0xFFFF7EB6),
        ground = Color(0xFF0A0D14), low = Color(0xFF0F1420), container = Color(0xFF141A28), high = Color(0xFF1A2232), highest = Color(0xFF222B3E),
        outline = Color(0xFF4C5E80), hairline = Color(0xFF233049),
        stripe = listOf(Color(0xFF5FD8EE), Color(0xFFA899FF), Color(0xFFFF7EB6), Color(0xFF5FD8EE))
    ),
    MAXIMUS(
        primary = Color(0xFFFFB000), onPrimary = Color(0xFF1A1000), secondary = Color(0xFF41FF7A), tertiary = Color(0xFFFF6B5B),
        ground = Color(0xFF050505), low = Color(0xFF0B0B09), container = Color(0xFF12110D), high = Color(0xFF1A1812), highest = Color(0xFF242117),
        outline = Color(0xFF6B5A2A), hairline = Color(0xFF2B2616),
        stripe = listOf(Color(0xFFFFB000), Color(0xFF41FF7A))
    );

    /** Gradient for titles and highlights. */
    val accentBrush: Brush get() = Brush.horizontalGradient(listOf(primary, secondary))
}

val LocalModuleStyle = staticCompositionLocalOf<ModuleStyle?> { null }

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

/** Applies a module's colour world (and, for Maximus, the terminal typography) to everything inside. */
@Composable
fun ModuleTheme(style: ModuleStyle, content: @Composable () -> Unit) {
    val base = MaterialTheme.colorScheme
    val baseType = MaterialTheme.typography
    val scheme = remember(style, base) {
        base.copy(
            primary = style.primary, onPrimary = style.onPrimary,
            primaryContainer = style.primary.copy(alpha = 0.22f).compositeOver(style.container), onPrimaryContainer = style.primary,
            secondary = style.secondary, secondaryContainer = style.secondary.copy(alpha = 0.18f).compositeOver(style.container), onSecondaryContainer = style.secondary,
            tertiary = style.tertiary, tertiaryContainer = style.tertiary.copy(alpha = 0.18f).compositeOver(style.container), onTertiaryContainer = style.tertiary,
            background = style.ground, surface = style.ground, surfaceDim = style.ground,
            surfaceContainerLowest = style.ground, surfaceContainerLow = style.low, surfaceContainer = style.container,
            surfaceContainerHigh = style.high, surfaceContainerHighest = style.highest, surfaceVariant = style.highest,
            surfaceTint = style.primary, outline = style.outline, outlineVariant = style.hairline
        )
    }
    val type = remember(style, baseType) { if (style == ModuleStyle.MAXIMUS) baseType.monospaced() else baseType }
    CompositionLocalProvider(LocalModuleStyle provides style) {
        MaterialTheme(colorScheme = scheme, typography = type, shapes = MaterialTheme.shapes, content = content)
    }
}

private fun Color.compositeOver(bg: Color): Color {
    val a = alpha
    return Color(red * a + bg.red * (1 - a), green * a + bg.green * (1 - a), blue * a + bg.blue * (1 - a), 1f)
}

/**
 * Decorative band under the top bar.
 *  • D&D: Landsknecht doublet — diagonal crimson/black panels with saffron and bone "slashes".
 *  • Strongman: glowing ember gradient. Nutrition: herb-to-saffron gradient with dots.
 *  • Lab: plasma gradient with oscilloscope ticks. Maximus: phosphor scan bar.
 */
@Composable
fun ModuleStripe(style: ModuleStyle, modifier: Modifier = Modifier) {
    val h = if (style == ModuleStyle.DND) 9.dp else 4.dp
    Canvas(modifier.fillMaxWidth().height(h)) {
        when (style) {
            ModuleStyle.DND -> {
                val red = style.stripe[0]; val black = style.stripe[1]; val gold = style.stripe[2]; val bone = style.stripe[3]
                val w = size.height * 2.2f
                clipRect {
                    var x = -size.height
                    var i = 0
                    while (x < size.width + size.height) {
                        val p = Path().apply {
                            moveTo(x, size.height); lineTo(x + size.height, 0f); lineTo(x + size.height + w, 0f); lineTo(x + w, size.height); close()
                        }
                        drawPath(p, if (i % 2 == 0) red else black)
                        // Landsknecht slashes: short bright cuts showing the lining.
                        val cx = x + w / 2 + size.height / 2
                        drawLine(if (i % 4 == 0) gold else bone, Offset(cx - 1.5f, size.height * 0.25f), Offset(cx + 1.5f, size.height * 0.75f), strokeWidth = size.height * 0.22f)
                        x += w
                        i++
                    }
                }
                drawLine(gold, Offset(0f, size.height - 0.5f), Offset(size.width, size.height - 0.5f), strokeWidth = 1f)
            }
            ModuleStyle.LAB -> {
                drawRect(Brush.horizontalGradient(style.stripe))
                var x = 0f
                while (x < size.width) { drawLine(style.ground.copy(alpha = 0.5f), Offset(x, 0f), Offset(x, size.height), 1f); x += 24f }
            }
            ModuleStyle.NUTRITION -> {
                drawRect(Brush.horizontalGradient(style.stripe))
                var x = 6f
                while (x < size.width) { drawCircle(style.ground.copy(alpha = 0.35f), size.height * 0.22f, Offset(x, size.height / 2)); x += 18f }
            }
            ModuleStyle.STRONGMAN -> drawRect(Brush.horizontalGradient(style.stripe + style.stripe.reversed()))
            ModuleStyle.MAXIMUS -> {
                drawRect(Brush.horizontalGradient(listOf(style.ground, style.primary, style.secondary, style.ground)))
                var x = 0f
                while (x < size.width) { drawRect(style.ground, Offset(x, 0f), androidx.compose.ui.geometry.Size(2f, size.height)); x += 6f }
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
fun KnightAvatar(size: Dp, modifier: Modifier = Modifier, phosphor: Boolean = true, border: Color? = null) {
    val shape = RoundedCornerShape(size * 0.22f)
    Image(
        painter = painterResource(R.drawable.knight_avatar),
        contentDescription = "Maximus",
        contentScale = ContentScale.Crop,
        colorFilter = if (phosphor) AmberPhosphor else null,
        modifier = modifier.size(size).clip(shape).let { if (border != null) it.border(1.dp, border, shape) else it }
    )
}
