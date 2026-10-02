package app.maximus.vault.data

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Copies secrets flagged as sensitive (hidden in the API-33 clipboard preview) and clears them after [CLEAR_MS]. */
object SecureClipboard {
    const val CLEAR_MS = 45_000L
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var pending: Job? = null

    fun copy(context: Context, label: String, text: String, sensitive: Boolean) {
        val cm = context.getSystemService(ClipboardManager::class.java) ?: return
        val clip = ClipData.newPlainText(label, text)
        if (sensitive) {
            clip.description.extras = PersistableBundle().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                else putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }
        cm.setPrimaryClip(clip)
        pending?.cancel()
        if (sensitive) {
            val app = context.applicationContext
            pending = scope.launch {
                delay(CLEAR_MS)
                runCatching { app.getSystemService(ClipboardManager::class.java)?.clearPrimaryClip() }
            }
        }
    }
}
