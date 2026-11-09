package com.mobilecomputing.applock

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Uygulama genelindeki kilit durumu. Süreç (process) yeniden başladığında
 * varsayılan değer "kilitli" olur, yani uygulama her soğuk açılışta kilitlidir.
 * Compose state olduğu için değiştiğinde ekran otomatik yeniden çizilir.
 */
object AppLock {
    var isLocked by mutableStateOf(true)
        private set

    private var backgroundedAtMs: Long? = null

    fun lock() {
        isLocked = true
    }

    fun unlock() {
        isLocked = false
        backgroundedAtMs = null
    }

    /** Activity.onStop: uygulama görünmez oldu. */
    fun onAppBackgrounded(nowMs: Long) {
        if (!isLocked) backgroundedAtMs = nowMs
    }

    /** Activity.onStart: uygulama tekrar görünür oldu. */
    fun onAppForegrounded(nowMs: Long, timeoutMs: Long) {
        val since = backgroundedAtMs ?: return
        backgroundedAtMs = null
        if (LockPolicy.shouldRelock(since, nowMs, timeoutMs)) isLocked = true
    }
}
