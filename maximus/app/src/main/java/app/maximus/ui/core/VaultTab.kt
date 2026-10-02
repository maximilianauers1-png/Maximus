@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.core

import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.core.app.AppServices
import app.maximus.core.crypto.PasswordGenerator
import app.maximus.core.crypto.PasswordPolicy
import app.maximus.core.crypto.Totp
import app.maximus.core.crypto.VaultSecret
import app.maximus.ui.components.SteelRule
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.GlyphIcon
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.fmt
import app.maximus.vault.data.SecureClipboard
import app.maximus.vault.data.VaultAuthRequest
import app.maximus.vault.data.VaultAuthenticator
import app.maximus.vault.data.VaultDraft
import app.maximus.vault.data.VaultEntryEntity
import app.maximus.vault.data.VaultProblem
import app.maximus.vault.data.VaultState
import app.maximus.vault.data.findActivity
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VaultTab(services: AppServices) {
    val session = services.vaultSession
    val state by session.state.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { session.refresh() }

    // Screenshots, screen recording and the recents thumbnail are blocked while secrets can be on screen.
    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }

    val promptTitle = stringResource(R.string.vault_prompt_title)
    val promptSubtitle = stringResource(R.string.vault_prompt_subtitle)
    val cancel = stringResource(R.string.cancel)
    fun authenticate(obtain: suspend () -> VaultAuthRequest?) {
        val act = activity ?: return
        scope.launch {
            error = null
            val req = withContext(Dispatchers.Default) { obtain() } ?: return@launch
            VaultAuthenticator.authenticate(
                act, req.cipher, promptTitle, promptSubtitle, cancel,
                onSuccess = { c -> scope.launch { req.complete(c) } },
                onError = { error = it }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp)) }
        when (val s = state) {
            VaultState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            VaultState.NotSetUp -> VaultGate(
                title = stringResource(R.string.vault_setup_title),
                text = stringResource(R.string.vault_setup_text),
                action = stringResource(R.string.vault_setup_action)
            ) { authenticate { session.beginWrap() } }
            VaultState.Locked -> VaultGate(
                title = stringResource(R.string.vault_locked_title),
                text = stringResource(R.string.vault_locked_text),
                action = stringResource(R.string.vault_unlock)
            ) { authenticate { session.beginUnlock() } }
            VaultState.NeedsRelink -> VaultGate(
                title = stringResource(R.string.vault_relink_title),
                text = stringResource(R.string.vault_relink_text),
                action = stringResource(R.string.vault_relink_action)
            ) { authenticate { session.beginWrap() } }
            is VaultState.Unavailable -> VaultProblemView(s.problem) { scope.launch { session.reset() } }
            VaultState.Unlocked -> VaultList(services)
        }
    }
}

@Composable
private fun VaultGate(title: String, text: String, action: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlyphIcon(Glyph.LOCK, tint = MaterialTheme.colorScheme.primary, size = 56.dp)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        SteelRule(modifier = Modifier.padding(vertical = 12.dp).size(width = 160.dp, height = 9.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAction) { Text(action) }
    }
}

@Composable
private fun VaultProblemView(problem: VaultProblem, onReset: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(
                when (problem) {
                    VaultProblem.NO_SECURE_LOCK -> R.string.vault_problem_lock
                    VaultProblem.KEY_INVALIDATED -> R.string.vault_problem_invalidated
                    VaultProblem.CORRUPT -> R.string.vault_problem_corrupt
                }
            ),
            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center
        )
        if (problem != VaultProblem.NO_SECURE_LOCK) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = { confirm = true }) { Text(stringResource(R.string.vault_reset)) }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(stringResource(R.string.vault_reset)) },
            text = { Text(stringResource(R.string.vault_reset_text)) },
            confirmButton = { TextButton(onClick = { confirm = false; onReset() }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

private sealed interface VaultDialog {
    data class Detail(val id: Long) : VaultDialog
    data class Edit(val draft: VaultDraft) : VaultDialog
}

private val EMPTY_DRAFT = VaultDraft(0, "", "", "", false, VaultSecret("", "", ""))

@Composable
private fun VaultList(services: AppServices) {
    val entries by services.vault.entries.collectAsState(initial = emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    var dialog by remember { mutableStateOf<VaultDialog?>(null) }
    val q = query.trim().lowercase()
    val filtered = entries.filter { q.isEmpty() || it.title.lowercase().contains(q) || it.username.lowercase().contains(q) || it.url.lowercase().contains(q) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    query, { query = it; services.vaultSession.touch() },
                    label = { Text(stringResource(R.string.search)) }, singleLine = true, modifier = Modifier.weight(1f)
                )
                GlyphButton(Glyph.LOCK, stringResource(R.string.vault_lock), { services.vaultSession.lock() }, tint = MaterialTheme.colorScheme.primary)
            }
        }
        item {
            Button(onClick = { dialog = VaultDialog.Edit(EMPTY_DRAFT) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.vault_new)) }
        }
        if (filtered.isEmpty()) item { EmptyState(stringResource(R.string.vault_empty)) }
        items(filtered, key = { it.id }) { e -> VaultRow(e) { dialog = VaultDialog.Detail(e.id) } }
    }

    when (val d = dialog) {
        null -> Unit
        is VaultDialog.Detail -> VaultDetailDialog(services, d.id, onClose = { dialog = null }, onEdit = { dialog = VaultDialog.Edit(it) })
        is VaultDialog.Edit -> VaultEditDialog(services, d.draft, onClose = { dialog = null })
    }
}

@Composable
private fun VaultRow(e: VaultEntryEntity, onClick: () -> Unit) {
    val ageDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - e.passwordChangedAt)
    PlateCard(onClick = onClick) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(e.title.ifBlank { e.url.ifBlank { "—" } }, style = MaterialTheme.typography.titleMedium)
                val sub = listOf(e.username, e.url).filter { it.isNotBlank() }.joinToString("  ")
                if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text(stringResource(R.string.vault_age, ageDays), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            if (e.favorite) GlyphIcon(Glyph.STAR_FILLED, tint = MaterialTheme.colorScheme.primary, size = 18.dp)
        }
    }
}

@Composable
private fun VaultDetailDialog(services: AppServices, id: Long, onClose: () -> Unit, onEdit: (VaultDraft) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf<VaultDraft?>(null) }
    var failed by remember { mutableStateOf(false) }
    var reveal by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(id) { draft = runCatching { services.vault.reveal(id) }.getOrNull(); failed = draft == null }
    val copied = stringResource(R.string.vault_copied)
    var toast by remember { mutableStateOf<String?>(null) }
    fun copy(label: String, text: String, sensitive: Boolean) {
        SecureClipboard.copy(context, label, text, sensitive); services.vaultSession.touch(); toast = copied
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(draft?.title ?: "", style = MaterialTheme.typography.headlineSmall) },
        text = {
            val d = draft
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (failed) Text(stringResource(R.string.vault_decrypt_failed), color = MaterialTheme.colorScheme.error)
                if (d != null) {
                    if (d.username.isNotBlank()) SecretLine(stringResource(R.string.vault_username), d.username, onCopy = { copy("user", d.username, false) })
                    if (d.secret.password.isNotEmpty()) {
                        SecretLine(
                            stringResource(R.string.vault_password),
                            if (reveal) d.secret.password else "•".repeat(12),
                            monospace = reveal,
                            onCopy = { copy("pw", d.secret.password, true) },
                            onReveal = { reveal = !reveal; services.vaultSession.touch() }
                        )
                    }
                    if (d.secret.totp.isNotBlank()) TotpLine(d.secret.totp) { code -> copy("otp", code, true) }
                    if (d.url.isNotBlank()) SecretLine(stringResource(R.string.vault_url), d.url, onCopy = { copy("url", d.url, false) })
                    if (d.secret.notes.isNotBlank()) {
                        Text(stringResource(R.string.vault_notes), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(d.secret.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                    toast?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary) }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { confirmDelete = true }) { Text(stringResource(R.string.delete)) }
                TextButton(enabled = draft != null, onClick = { draft?.let(onEdit) }) { Text(stringResource(R.string.edit)) }
                TextButton(onClick = onClose) { Text(stringResource(R.string.close)) }
            }
        }
    )
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.vault_delete_title)) },
            text = { Text(stringResource(R.string.vault_delete_text)) },
            confirmButton = { TextButton(onClick = { scope.launch { services.vault.delete(id); confirmDelete = false; onClose() } }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun SecretLine(label: String, value: String, monospace: Boolean = false, onCopy: () -> Unit, onReveal: (() -> Unit)? = null) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge.let { if (monospace) it.copy(fontFamily = FontFamily.Monospace) else it },
                modifier = Modifier.weight(1f)
            )
            if (onReveal != null) GlyphButton(Glyph.EYE, stringResource(R.string.vault_reveal), onReveal, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            GlyphButton(Glyph.COPY, stringResource(R.string.vault_copy), onCopy, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TotpLine(spec: String, onCopy: (String) -> Unit) {
    val params = remember(spec) { Totp.parse(spec) }
    if (params == null) {
        Text(stringResource(R.string.vault_totp_invalid), color = MaterialTheme.colorScheme.error)
        return
    }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(params) { while (true) { now = System.currentTimeMillis(); delay(1000L - now % 1000L) } }
    val code = Totp.at(params, now)
    val grouped = if (code.code.length == 6) code.code.chunked(3).joinToString(" ") else code.code.chunked(4).joinToString(" ")
    Column {
        Text(
            stringResource(R.string.vault_totp) + if (params.issuer.isNotBlank()) " · ${params.issuer}" else "",
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(grouped, style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { code.secondsRemaining / code.periodSeconds.toFloat() },
                    modifier = Modifier.size(30.dp), strokeWidth = 2.dp,
                    color = if (code.secondsRemaining <= 5) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                )
                Text("${code.secondsRemaining}", style = MaterialTheme.typography.labelSmall)
            }
            GlyphButton(Glyph.COPY, stringResource(R.string.vault_copy), { onCopy(code.code) }, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun VaultEditDialog(services: AppServices, initial: VaultDraft, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf(initial.title) }
    var user by remember { mutableStateOf(initial.username) }
    var url by remember { mutableStateOf(initial.url) }
    var password by remember { mutableStateOf(initial.secret.password) }
    var totp by remember { mutableStateOf(initial.secret.totp) }
    var notes by remember { mutableStateOf(initial.secret.notes) }
    var favorite by remember { mutableStateOf(initial.favorite) }
    var show by remember { mutableStateOf(false) }
    var generator by remember { mutableStateOf(false) }
    val totpValid = totp.isBlank() || Totp.parse(totp) != null

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(if (initial.id == 0L) R.string.vault_new else R.string.vault_edit)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.vault_title)) }, singleLine = true)
                OutlinedTextField(user, { user = it }, label = { Text(stringResource(R.string.vault_username)) }, singleLine = true)
                OutlinedTextField(url, { url = it }, label = { Text(stringResource(R.string.vault_url)) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
                OutlinedTextField(
                    password, { password = it }, label = { Text(stringResource(R.string.vault_password)) }, singleLine = true,
                    visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = { GlyphButton(Glyph.EYE, stringResource(R.string.vault_reveal), { show = !show }, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                )
                if (password.isNotEmpty()) {
                    Text(
                        stringResource(R.string.vault_strength_upper, fmt(PasswordGenerator.upperBoundBits(password), 0)),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = { generator = true }) { Text(stringResource(R.string.vault_generate)) }
                OutlinedTextField(
                    totp, { totp = it }, label = { Text(stringResource(R.string.vault_totp_field)) }, singleLine = true,
                    isError = !totpValid,
                    supportingText = { Text(stringResource(if (totpValid) R.string.vault_totp_hint else R.string.vault_totp_invalid)) }
                )
                OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.vault_notes)) }, minLines = 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(favorite, { favorite = it })
                    Text(stringResource(R.string.vault_favorite))
                }
            }
        },
        confirmButton = {
            TextButton(enabled = (title.isNotBlank() || url.isNotBlank()) && totpValid, onClick = {
                scope.launch {
                    services.vault.save(VaultDraft(initial.id, title.trim(), user.trim(), url.trim(), favorite, VaultSecret(password, totp.trim(), notes)))
                    onClose()
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.cancel)) } }
    )
    if (generator) GeneratorDialog(onDismiss = { generator = false }) { password = it; show = true; generator = false }
}

@Composable
private fun GeneratorDialog(onDismiss: () -> Unit, onUse: (String) -> Unit) {
    var length by rememberSaveable { mutableIntStateOf(20) }
    var lower by rememberSaveable { mutableStateOf(true) }
    var upper by rememberSaveable { mutableStateOf(true) }
    var digits by rememberSaveable { mutableStateOf(true) }
    var symbols by rememberSaveable { mutableStateOf(true) }
    var noAmbiguous by rememberSaveable { mutableStateOf(false) }
    var seed by remember { mutableIntStateOf(0) }
    val policy = PasswordPolicy(length, lower, upper, digits, symbols, noAmbiguous)
    val valid = PasswordGenerator.classes(policy).isNotEmpty()
    val random = remember { SecureRandom() }
    val password = remember(policy, seed) { if (valid) PasswordGenerator.generate(policy, random) else "" }
    val bits = remember(policy) { PasswordGenerator.entropyBits(policy) }
    val locale = LocalConfiguration.current.locales[0]
    val (amount, unit) = splitDuration(PasswordGenerator.expectedCrackSeconds(bits))
    val unitLabel = stringResource(
        when (unit) {
            DurationUnit.SECONDS -> R.string.unit_seconds
            DurationUnit.MINUTES -> R.string.unit_minutes
            DurationUnit.HOURS -> R.string.unit_hours
            DurationUnit.DAYS -> R.string.unit_days
            DurationUnit.YEARS -> R.string.unit_years
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.vault_generator)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PlateCard {
                    Text(
                        password, style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(14.dp)
                    )
                }
                Text(stringResource(R.string.vault_gen_length, length), style = MaterialTheme.typography.labelLarge)
                Slider(value = length.toFloat(), onValueChange = { length = it.toInt() }, valueRange = 8f..64f, steps = 55)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(lower, { lower = !lower }, label = { Text("a–z") })
                    FilterChip(upper, { upper = !upper }, label = { Text("A–Z") })
                    FilterChip(digits, { digits = !digits }, label = { Text("0–9") })
                    FilterChip(symbols, { symbols = !symbols }, label = { Text("!#%…") })
                    FilterChip(noAmbiguous, { noAmbiguous = !noAmbiguous }, label = { Text(stringResource(R.string.vault_gen_unambiguous)) })
                }
                Text(stringResource(R.string.vault_gen_entropy, fmt(bits, 1)), style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.vault_gen_crack, scientific(amount, locale), unitLabel),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(enabled = valid, onClick = { seed++ }) { Text(stringResource(R.string.vault_gen_again)) }
                TextButton(enabled = valid, onClick = { onUse(password) }) { Text(stringResource(R.string.vault_gen_use)) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
