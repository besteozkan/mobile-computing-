package com.mobilecomputing.applock

import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity

/**
 * BiometricPrompt bir FragmentActivity gerektirir (FragmentActivity de bir
 * ComponentActivity olduğu için setContent ile Compose kullanılabilir).
 */
class MainActivity : FragmentActivity() {

    private lateinit var store: PinStore
    private lateinit var biometric: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ekran görüntüsü / ekran kaydı engellenir ve "son uygulamalar"
        // listesinde içerik gizlenir.
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        store = PinStore(this)
        biometric = BiometricHelper(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(store, biometric)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // elapsedRealtime: kullanıcı telefon saatini değiştirse de etkilenmez.
        AppLock.onAppForegrounded(SystemClock.elapsedRealtime(), store.lockTimeout.ms)
    }

    override fun onStop() {
        super.onStop()
        // Ekran döndürme gibi yapılandırma değişikliklerinde kilitleme.
        if (!isChangingConfigurations) {
            AppLock.onAppBackgrounded(SystemClock.elapsedRealtime())
        }
    }
}
