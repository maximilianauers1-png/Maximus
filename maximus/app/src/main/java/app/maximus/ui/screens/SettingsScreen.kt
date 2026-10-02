package app.maximus.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import app.maximus.R
import app.maximus.core.app.AppServices
import app.maximus.core.backup.BackupResult
import app.maximus.core.crypto.PasswordGenerator
import app.maximus.ui.components.MaximusTopBar
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.strongman.fmt
import app.maximus.vault.data.VaultState
import java.time.LocalDate
import kotlinx.coroutines.launch

private enum class LanguageChoice(val tag: String) { SYSTEM(""), GERMAN("de"), ENGLISH("en") }

private const val MIN_PASSPHRASE = 12

@Composable
fun SettingsScreen(onBack: () -> Unit, services: AppServices) {
    var selected by remember {
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        mutableStateOf(LanguageChoice.entries.firstOrNull { it.tag.isNotEmpty() && current.startsWith(it.tag) } ?: LanguageChoice.SYSTEM)
    }
    Scaffold(topBar = { MaximusTopBar(stringResource(R.string.nav_settings), onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(stringResource(R.string.settings_language)) {
                LanguageChoice.entries.forEach { choice ->
                    val label = when (choice) {
                        LanguageChoice.SYSTEM -> stringResource(R.string.language_system)
                        LanguageChoice.GERMAN -> stringResource(R.string.language_de)
                        LanguageChoice.ENGLISH -> stringResource(R.string.language_en)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == choice,
                                role = Role.RadioButton,
                                onClick = {
                                    selected = choice
                                    AppCompatDelegate.setApplicationLocales(
                                        if (choice.tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
                                        else LocaleListCompat.forLanguageTags(choice.tag)
                                    )
                                }
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected == choice, onClick = null)
                        Text(label, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
            BackupCard(services)
        }
    }
}

private sealed interface BackupDialog {
    data class Restore(val uri: Uri) : BackupDialog
}

@Composable
private fun BackupCard(services: AppServices) {
    val scope = rememberCoroutineScope()
    val vaultState by services.vaultSession.state.collectAsState()
    LaunchedEffect(Unit) { services.vaultSession.refresh() }
    var pass1 by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<BackupDialog?>(null) }

    val msgExported = stringResource(R.string.backup_exported)
    val msgExportedNoKey = stringResource(R.string.backup_exported_no_key)
    val msgRestored = stringResource(R.string.backup_restored)
    val msgRestoredRelink = stringResource(R.string.backup_restored_relink)
    val msgWrong = stringResource(R.string.backup_wrong_pass)
    val msgVersion = stringResource(R.string.backup_version)
    val msgFailed = stringResource(R.string.backup_failed)

    fun describe(r: BackupResult): String = when (r) {
        is BackupResult.Exported -> if (r.includesVaultKey) msgExported else msgExportedNoKey
        is BackupResult.Restored -> if (r.vaultNeedsRelink) msgRestoredRelink else msgRestored
        BackupResult.WrongPassphrase -> msgWrong
        is BackupResult.IncompatibleVersion -> msgVersion.format(r.found, r.expected)
        is BackupResult.Failed -> msgFailed.format(r.reason)
    }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri == null) { services.vaultSession.takeExportGrant()?.fill(0); return@rememberLauncherForActivityResult }
        busy = true
        val chars = pass1.toCharArray()
        scope.launch {
            val r = services.backup.export(uri, chars)
            chars.fill('\u0000')
            pass1 = ""; pass2 = ""
            message = describe(r); busy = false
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) dialog = BackupDialog.Restore(uri)
    }

    val vaultLocked = vaultState == VaultState.Locked
    val strong = pass1.length >= MIN_PASSPHRASE
    val match = pass1 == pass2

    SectionCard(stringResource(R.string.backup_title)) {
        Text(stringResource(R.string.backup_explain), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            pass1, { pass1 = it }, label = { Text(stringResource(R.string.backup_passphrase)) }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            pass2, { pass2 = it }, label = { Text(stringResource(R.string.backup_passphrase_repeat)) }, singleLine = true,
            isError = pass2.isNotEmpty() && !match,
            visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        if (pass1.isNotEmpty()) {
            Text(
                stringResource(R.string.backup_strength, fmt(PasswordGenerator.upperBoundBits(pass1), 0), MIN_PASSPHRASE),
                style = MaterialTheme.typography.bodySmall,
                color = if (strong) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
            )
        }
        if (vaultLocked) {
            Text(stringResource(R.string.backup_vault_locked), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            Button(
                enabled = !busy && strong && match && !vaultLocked,
                onClick = {
                    services.vaultSession.createExportGrant()
                    exporter.launch("maximus-${LocalDate.now()}.mxb")
                }
            ) { Text(stringResource(R.string.backup_export)) }
            OutlinedButton(enabled = !busy, onClick = { importer.launch(arrayOf("*/*")) }) { Text(stringResource(R.string.backup_import)) }
        }
        message?.let { Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp)) }
    }

    when (val d = dialog) {
        null -> Unit
        is BackupDialog.Restore -> {
            var pass by remember(d) { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { dialog = null },
                title = { Text(stringResource(R.string.backup_restore_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.backup_restore_warning))
                        OutlinedTextField(
                            pass, { pass = it }, label = { Text(stringResource(R.string.backup_passphrase)) }, singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                    }
                },
                confirmButton = {
                    TextButton(enabled = pass.isNotEmpty(), onClick = {
                        val chars = pass.toCharArray()
                        pass = ""
                        dialog = null
                        busy = true
                        scope.launch {
                            val r = services.backup.restore(d.uri, chars)
                            chars.fill('\u0000')
                            message = describe(r); busy = false
                        }
                    }) { Text(stringResource(R.string.backup_restore_confirm)) }
                },
                dismissButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.cancel)) } }
            )
        }
    }
}
