package app.maximus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.ui.components.MaximusTopBar
import app.maximus.core.security.KeySecurityLevel
import app.maximus.diagnostics.DiagnosticsRepository
import app.maximus.diagnostics.DiagnosticsSnapshot
import app.maximus.diagnostics.EncryptionState
import java.util.Locale
import kotlinx.coroutines.CancellationException

private sealed interface DiagState {
    data object Loading : DiagState
    data class Ready(val snapshot: DiagnosticsSnapshot) : DiagState
    data class Failed(val message: String) : DiagState
}

/** C1 peak budget: 3.0 GB = 3.0e9 bytes (PSS is reported in KiB). */
private const val C1_PEAK_BUDGET_BYTES = 3_000_000_000L

private fun formatBytes(bytes: Long): String {
    val mib = bytes / (1024.0 * 1024.0)
    return if (mib >= 1024.0) String.format(Locale.ROOT, "%.2f GiB", mib / 1024.0)
    else String.format(Locale.ROOT, "%.1f MiB", mib)
}

private fun formatKb(kb: Long?): String = if (kb == null) "–" else formatBytes(kb * 1024L)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(repository: DiagnosticsRepository, onBack: () -> Unit) {
    var refreshCounter by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf<DiagState>(DiagState.Loading) }

    LaunchedEffect(refreshCounter) {
        state = DiagState.Loading
        state = try {
            DiagState.Ready(repository.snapshot())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DiagState.Failed(e.javaClass.simpleName)
        }
    }

    Scaffold(
        topBar = {
            MaximusTopBar(title = stringResource(R.string.diag_title), onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (val s = state) {
                DiagState.Loading -> Text(stringResource(R.string.diag_loading))
                is DiagState.Failed -> Text(stringResource(R.string.diag_error, s.message), color = MaterialTheme.colorScheme.error)
                is DiagState.Ready -> SnapshotView(s.snapshot)
            }
            Button(onClick = { refreshCounter++ }) { Text(stringResource(R.string.diag_refresh)) }
        }
    }
}

@Composable
private fun SnapshotView(s: DiagnosticsSnapshot) {
    val yes = stringResource(R.string.yes)
    val no = stringResource(R.string.no)
    val budgetPercent = String.format(Locale.ROOT, "%.1f %%", 100.0 * s.totalPssKb * 1024.0 / C1_PEAK_BUDGET_BYTES)
    val encryption = when (s.databaseEncryption) {
        EncryptionState.ENCRYPTED -> yes
        EncryptionState.PLAINTEXT -> no
        EncryptionState.UNDETERMINED -> stringResource(R.string.undetermined)
    }
    val keySecurity = when (s.keySecurity) {
        KeySecurityLevel.STRONGBOX -> stringResource(R.string.key_security_strongbox)
        KeySecurityLevel.TRUSTED_ENVIRONMENT -> stringResource(R.string.key_security_tee)
        KeySecurityLevel.SECURE_HARDWARE -> stringResource(R.string.key_security_secure_hw)
        KeySecurityLevel.SOFTWARE -> stringResource(R.string.key_security_software)
        KeySecurityLevel.UNKNOWN -> stringResource(R.string.key_security_unknown)
    }
    Text(stringResource(R.string.diag_pss, formatKb(s.totalPssKb)), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.diag_budget, budgetPercent))
    Text(stringResource(R.string.diag_java_heap, formatKb(s.javaHeapKb)))
    Text(stringResource(R.string.diag_native_heap, formatKb(s.nativeHeapKb)))
    Text(stringResource(R.string.diag_device_ram, formatBytes(s.deviceAvailableBytes), formatBytes(s.deviceTotalBytes)))
    Text(stringResource(R.string.diag_low_memory, if (s.lowMemory) yes else no))
    Text(stringResource(R.string.diag_db_encrypted, encryption))
    Text(stringResource(R.string.diag_cipher_version, s.cipherVersion ?: "–"))
    Text(stringResource(R.string.diag_key_security, keySecurity))
    Text(stringResource(R.string.diag_resident, s.residentHeavyComponent ?: stringResource(R.string.diag_none)))
}
