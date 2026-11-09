package com.mobilecomputing.applock

import android.content.Context
import java.util.Base64

sealed interface PinCheckResult {
    data object Success : PinCheckResult
    data class Wrong(val attemptsLeft: Int) : PinCheckResult
    data class LockedOut(val untilMs: Long) : PinCheckResult
}

/** Kilit otomatik olarak ne zaman devreye girsin? */
enum class LockTimeout(val label: String, val ms: Long) {
    Immediately("Hemen", 0),
    Sec30("30 saniye", 30_000),
    Min1("1 dakika", 60_000),
    Min5("5 dakika", 5 * 60_000),
}

/**
 * PIN özeti, hatalı deneme sayacı, ayarlar ve örnek "gizli not" SharedPreferences'ta
 * saklanır. Gerçek bir uygulamada gizli veriler Android Keystore ile şifrelenmelidir
 * (README'ye bakın).
 */
class PinStore(context: Context) {
    private val prefs = context.getSharedPreferences("app_lock", Context.MODE_PRIVATE)
    private val b64Enc = Base64.getEncoder()
    private val b64Dec = Base64.getDecoder()

    val hasPin: Boolean get() = prefs.contains(KEY_HASH)

    fun setPin(pin: String) {
        require(PinRules.isValidFormat(pin))
        val salt = PinHasher.newSalt()
        prefs.edit()
            .putString(KEY_SALT, b64Enc.encodeToString(salt))
            .putString(KEY_HASH, b64Enc.encodeToString(PinHasher.hash(pin, salt)))
            .putInt(KEY_FAILS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0)
            .apply()
    }

    /** Deneme sayacını etkilemeden kontrol (PIN değiştirirken kullanılır). */
    fun matches(pin: String): Boolean {
        val salt = prefs.getString(KEY_SALT, null) ?: return false
        val hash = prefs.getString(KEY_HASH, null) ?: return false
        return PinHasher.matches(pin, b64Dec.decode(salt), b64Dec.decode(hash))
    }

    /** Kilit ekranında PIN kontrolü: hatalı denemeleri sayar, gerekirse bekletir. */
    fun checkPin(pin: String, nowMs: Long): PinCheckResult {
        val until = lockoutUntilMs
        if (nowMs < until) return PinCheckResult.LockedOut(until)

        if (matches(pin)) {
            resetFailures()
            return PinCheckResult.Success
        }

        val fails = prefs.getInt(KEY_FAILS, 0) + 1
        val lockout = LockPolicy.lockoutDurationMs(fails)
        val newUntil = if (lockout > 0) nowMs + lockout else 0L
        prefs.edit().putInt(KEY_FAILS, fails).putLong(KEY_LOCKOUT_UNTIL, newUntil).apply()

        return if (lockout > 0) PinCheckResult.LockedOut(newUntil)
        else PinCheckResult.Wrong(LockPolicy.FREE_ATTEMPTS - fails)
    }

    fun resetFailures() {
        prefs.edit().putInt(KEY_FAILS, 0).putLong(KEY_LOCKOUT_UNTIL, 0).apply()
    }

    val lockoutUntilMs: Long get() = prefs.getLong(KEY_LOCKOUT_UNTIL, 0)

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply()

    var lockTimeout: LockTimeout
        get() = LockTimeout.entries.firstOrNull { it.name == prefs.getString(KEY_TIMEOUT, null) }
            ?: LockTimeout.Immediately
        set(value) = prefs.edit().putString(KEY_TIMEOUT, value.name).apply()

    var secretNote: String
        get() = prefs.getString(KEY_NOTE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_NOTE, value).apply()

    private companion object {
        const val KEY_SALT = "pin_salt"
        const val KEY_HASH = "pin_hash"
        const val KEY_FAILS = "failed_attempts"
        const val KEY_LOCKOUT_UNTIL = "lockout_until"
        const val KEY_BIOMETRIC = "biometric_enabled"
        const val KEY_TIMEOUT = "lock_timeout"
        const val KEY_NOTE = "secret_note"
    }
}
