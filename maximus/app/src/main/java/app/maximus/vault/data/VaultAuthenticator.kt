package app.maximus.vault.data

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import javax.crypto.Cipher

/**
 * Platform BiometricPrompt (no androidx.biometric dependency; minSdk 29).
 * API ≥ 30: BIOMETRIC_STRONG or device credential, bound to the CryptoObject.
 * API 29: device credential cannot be combined with a CryptoObject, so strong biometrics only.
 */
object VaultAuthenticator {
    /** BiometricConstants.BIOMETRIC_ERROR_NEGATIVE_BUTTON: not public in the platform BiometricPrompt API. */
    private const val ERROR_NEGATIVE_BUTTON = 13

    fun authenticate(
        activity: Activity,
        cipher: Cipher,
        title: String,
        subtitle: String,
        cancelLabel: String,
        onSuccess: (Cipher) -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = activity.mainExecutor
        val builder = BiometricPrompt.Builder(activity).setTitle(title).setSubtitle(subtitle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        } else {
            builder.setNegativeButton(cancelLabel, executor) { _, _ -> }
        }
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess(result.cryptoObject?.cipher ?: cipher)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED &&
                    errorCode != ERROR_NEGATIVE_BUTTON
                ) onError(errString.toString())
            }
        }
        builder.build().authenticate(BiometricPrompt.CryptoObject(cipher), CancellationSignal(), executor, callback)
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
