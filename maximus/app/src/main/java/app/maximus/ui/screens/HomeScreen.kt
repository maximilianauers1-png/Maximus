package app.maximus.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.maximus.R
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.GlyphIcon
import app.maximus.ui.components.PlateCard
import app.maximus.ui.navigation.Routes
import app.maximus.ui.theme.BrandArtwork
import app.maximus.ui.theme.Design

private data class ModuleEntry(@StringRes val title: Int, @StringRes val description: Int, val route: String, val icon: Glyph)

private val modules = listOf(
    ModuleEntry(R.string.module_strongman, R.string.home_strongman, Routes.STRONGMAN, Glyph.BARBELL),
    ModuleEntry(R.string.module_nutrition, R.string.home_nutrition, Routes.NUTRITION, Glyph.BOWL),
    ModuleEntry(R.string.module_chat, R.string.home_chat, Routes.CHAT, Glyph.CHIP),
    ModuleEntry(R.string.module_core, R.string.home_core, Routes.CORE, Glyph.BOOK),
    ModuleEntry(R.string.module_trainer, R.string.home_lab, Routes.LAB, Glyph.FLASK),
    ModuleEntry(R.string.module_simlab, R.string.home_simulation, Routes.SIMULATIONS, Glyph.PULSE),
    ModuleEntry(R.string.module_ai, R.string.home_ai, Routes.AI, Glyph.CHIP),
    ModuleEntry(R.string.module_dnd, R.string.home_dnd, Routes.DND, Glyph.DICE)
)

@Composable
fun HomeScreen(onOpenSettings: () -> Unit, onOpenDiagnostics: () -> Unit, onOpenModule: (String) -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 148.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { Hero(onOpenSettings, onOpenDiagnostics) }
            items(modules, key = { it.route }) { module ->
                ModuleCard(module) { onOpenModule(module.route) }
            }
        }
    }
}

@Composable
private fun Hero(onOpenSettings: () -> Unit, onOpenDiagnostics: () -> Unit) {
    val artwork = Design.artwork
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val heroHeight = (maxWidth * 0.94f).coerceIn(290.dp, 400.dp)
        Box(Modifier.fillMaxWidth().height(heroHeight).clip(MaterialTheme.shapes.large)) {
            Image(
                painterResource(artwork.hero), contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                alignment = if (artwork == BrandArtwork.ATHLETE) Alignment.TopCenter else Alignment.Center
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0f to Color.Transparent, 0.5f to Color.Transparent, 1f to Color(0xED110E0D))
                )
            )
            Surface(
                Modifier.align(Alignment.TopEnd).padding(10.dp),
                color = colors.background.copy(alpha = 0.83f),
                shape = MaterialTheme.shapes.medium
            ) {
                Row {
                    GlyphButton(Glyph.PULSE, stringResource(R.string.nav_diagnostics), onOpenDiagnostics)
                    GlyphButton(Glyph.GEAR, stringResource(R.string.nav_settings), onOpenSettings)
                }
            }
            Text(
                "MAXIMUS",
                Modifier.align(Alignment.BottomStart).padding(horizontal = 18.dp, vertical = 16.dp).semantics { heading() },
                color = colors.onBackground,
                style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 1.3.sp)
            )
        }
    }
}

@Composable
private fun ModuleCard(module: ModuleEntry, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    PlateCard(onClick = onClick) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = 122.dp).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                GlyphIcon(module.icon, tint = colors.primary, size = 22.dp)
                GlyphIcon(Glyph.CHEVRON_RIGHT, tint = colors.outline, size = 15.dp)
            }
            Text(stringResource(module.title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(module.description), style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}
