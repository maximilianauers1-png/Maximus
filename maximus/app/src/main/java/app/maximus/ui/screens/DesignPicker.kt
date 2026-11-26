package app.maximus.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.core.app.DesignRepository
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.theme.BrandArtwork
import app.maximus.ui.theme.Design
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun DesignPicker(repository: DesignRepository) {
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    SectionCard(stringResource(R.string.design_artwork_title)) {
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BrandArtwork.entries.forEach { artwork ->
                val selected = artwork == Design.artwork
                Row(
                    Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small)
                        .selectable(selected, enabled = !saving && !selected, role = Role.RadioButton) {
                            saving = true
                            failed = false
                            scope.launch {
                                try {
                                    repository.select(artwork)
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (_: Exception) {
                                    failed = true
                                } finally {
                                    saving = false
                                }
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painterResource(artwork.avatar), contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.small), contentScale = ContentScale.Crop
                    )
                    Text(stringResource(artwork.label), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    RadioButton(selected = selected, onClick = null)
                }
            }
            if (failed) Text(stringResource(R.string.design_artwork_save_error), color = MaterialTheme.colorScheme.error)
        }
    }
}
