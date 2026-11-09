package com.mobilecomputing.applock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * androidx.biometric sarmalayıcısı. BiometricPrompt, Activity.onCreate sırasında
 * oluşturulmalıdır; bu yüzden nesne MainActivity.onCreate içinde yaratılır ve
 * sonuçlar o anki dinleyicilere iletilir.
 */
class BiometricHelper(private val activity: FragmentActivity) {

    private var onSuccess: () -> Unit = {}
    private var onError: (String) -> Unit = {}

    private val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Kullanıcı "PIN kullan"a bastıysa ya da iptal ettiyse hata gösterme.
                val userCancelled = errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                if (!userCancelled) onError(errString.toString())
            }
            // onAuthenticationFailed: tanınmayan parmak izi; sistem diyaloğu zaten
            // tekrar denetir, burada bir şey yapmaya gerek yok.
        },
    )

    /** Cihazda biyometrik donanım var ve en az bir parmak izi/yüz kayıtlı mı? */
    fun isAvailable(): Boolean =
        BiometricManager.from(activity).canAuthenticate(BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS

    fun authenticate(onSuccess: () -> Unit, onError: (String) -> Unit) {
        this.onSuccess = onSuccess
        this.onError = onError
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Kilidi aç")
            .setSubtitle("Parmak izi veya yüz tanıma kullanın")
            .setNegativeButtonText("PIN kullan")
            .setAllowedAuthenticators(BIOMETRIC_WEAK)
            .build()
        prompt.authenticate(info)
    }
}
