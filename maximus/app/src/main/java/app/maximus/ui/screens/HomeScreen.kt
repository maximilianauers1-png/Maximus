package app.maximus.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.GlyphIcon
import app.maximus.ui.components.PlateCard
import app.maximus.ui.components.SteelRule
import app.maximus.ui.navigation.Routes
import app.maximus.ui.theme.KnightAvatar
import app.maximus.ui.theme.ModuleStyle
import app.maximus.ui.theme.Palette
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** [phase] is the build phase; [route] is non-null once the module is implemented. */
private data class ModuleEntry(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val phase: Int,
    val route: String? = null,
    /** Colour world of the module, previewed on the home screen as accent bar and icon colour. */
    val style: ModuleStyle? = null
)

private val MODULES = listOf(
    ModuleEntry(R.string.module_core, R.string.module_core_desc, 1, Routes.CORE),
    ModuleEntry(R.string.module_strongman, R.string.module_strongman_desc, 2, Routes.STRONGMAN, ModuleStyle.STRONGMAN),
    ModuleEntry(R.string.module_nutrition, R.string.module_nutrition_desc, 3, Routes.NUTRITION, ModuleStyle.NUTRITION),
    ModuleEntry(R.string.module_dnd, R.string.module_dnd_desc, 4, Routes.DND, ModuleStyle.DND),
    ModuleEntry(R.string.module_trainer, R.string.module_trainer_desc, 5, Routes.LAB, ModuleStyle.LAB),
    ModuleEntry(R.string.module_chat, R.string.module_chat_desc, 6, Routes.CHAT, ModuleStyle.MAXIMUS)
)

@Composable
fun HomeScreen(onOpenSettings: () -> Unit, onOpenDiagnostics: () -> Unit, onOpenModule: (String) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val today = remember { LocalDate.now() }
    val dateLine = remember(locale, today) {
        today.format(DateTimeFormatter.ofPattern(if (locale.language == "de") "EEEE, d. MMMM yyyy" else "EEEE, d MMMM yyyy", locale))
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Hero(dateLine, onOpenSettings, onOpenDiagnostics) }
            items(MODULES) { module -> Box(Modifier.padding(horizontal = 20.dp)) { ModuleRow(module, onOpenModule) } }
        }
    }
}

/**
 * Full-bleed knight image (1080 × 776 px WebP, decoded ≈ 3.4 MB) whose black background merges with the
 * iron ground; a vertical gradient fades the lower third into the background so the wordmark sits on it.
 */
@Composable
private fun Hero(dateLine: String, onOpenSettings: () -> Unit, onOpenDiagnostics: () -> Unit) {
    val ground = MaterialTheme.colorScheme.background
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1080f / 776f)) {
        Image(
            painter = painterResource(R.drawable.knight_hero),
            contentDescription = stringResource(R.string.mascot_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Color.Transparent, 0.55f to Color.Transparent, 0.82f to ground.copy(alpha = 0.85f), 1f to ground)
            )
        )
        Row(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
            GlyphButton(Glyph.PULSE, stringResource(R.string.nav_diagnostics), onOpenDiagnostics, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            GlyphButton(Glyph.GEAR, stringResource(R.string.nav_settings), onOpenSettings, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                "Maximus",
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.verticalGradient(listOf(Palette.SteelLight, Palette.Steel, Palette.SteelDeep))
                )
            )
            SteelRule(lozenge = false, modifier = Modifier.width(220.dp).padding(vertical = 2.dp))
            Text(stringResource(R.string.home_motto), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(dateLine, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModuleRow(module: ModuleEntry, onOpen: (String) -> Unit) {
    val route = module.route
    val active = route != null
    val content: @Composable () -> Unit = {
        val accent = module.style?.primary ?: MaterialTheme.colorScheme.primary
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            // Accent bar in the module's colour world (D&D shows its Landsknecht stripes).
            Box(Modifier.width(4.dp).fillMaxHeight().background(module.style?.let { Brush.verticalGradient(it.stripe) } ?: Brush.verticalGradient(listOf(accent, accent))))
            Row(modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                if (module.style == ModuleStyle.MAXIMUS) KnightAvatar(34.dp, border = accent)
                else GlyphIcon(Glyph.SHIELD, tint = if (active) accent else MaterialTheme.colorScheme.outline, size = 30.dp)
                Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(
                        stringResource(module.title),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (active) stringResource(module.description) else stringResource(R.string.phase_label, "P${module.phase}"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (active) GlyphIcon(Glyph.CHEVRON_RIGHT, tint = accent, size = 20.dp)
            }
        }
    }
    if (active) PlateCard(onClick = { onOpen(route!!) }) { content() } else PlateCard { content() }
}
