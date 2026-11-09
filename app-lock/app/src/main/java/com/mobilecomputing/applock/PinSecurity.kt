package com.mobilecomputing.applock

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** PIN uzunluğu (hane sayısı). */
const val PIN_LENGTH = 4

/**
 * PIN asla düz metin olarak saklanmaz. Rastgele "salt" ile PBKDF2 özeti (hash)
 * alınır ve sadece özet saklanır. Bu dosyadaki her şey saf Kotlin/Java olduğu
 * için Android olmadan birim testlerle denenebilir.
 */
object PinHasher {
    private const val ITERATIONS = 10_000
    private const val KEY_BITS = 256

    fun newSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }

    fun hash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_BITS)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    /** Sabit zamanlı karşılaştırma: zamanlama saldırılarına karşı. */
    fun matches(pin: String, salt: ByteArray, expectedHash: ByteArray): Boolean =
        MessageDigest.isEqual(hash(pin, salt), expectedHash)
}

/** Kolay tahmin edilen PIN'leri reddeder. */
object PinRules {
    fun isValidFormat(pin: String): Boolean =
        pin.length == PIN_LENGTH && pin.all { it.isDigit() }

    /** 1111 gibi tekrarlar ya da 1234 / 4321 gibi ardışık diziler. */
    fun isTooSimple(pin: String): Boolean {
        if (pin.toSet().size == 1) return true
        val steps = pin.zipWithNext { a, b -> b - a }.toSet()
        return steps == setOf(1) || steps == setOf(-1)
    }
}

/** Hatalı denemeler ve otomatik kilitleme kuralları. */
object LockPolicy {
    /** Bu kadar hatalı denemeden sonra bekleme süresi başlar. */
    const val FREE_ATTEMPTS = 5
    const val BASE_LOCKOUT_MS = 30_000L
    const val MAX_LOCKOUT_MS = 5 * 60_000L

    /**
     * 5. hatada 30 sn, sonra her hatada süre ikiye katlanır (60, 120, 240 sn),
     * en fazla 5 dakika.
     */
    fun lockoutDurationMs(failedAttempts: Int): Long {
        if (failedAttempts < FREE_ATTEMPTS) return 0
        val exponent = (failedAttempts - FREE_ATTEMPTS).coerceAtMost(10)
        return (BASE_LOCKOUT_MS shl exponent).coerceAtMost(MAX_LOCKOUT_MS)
    }

    /** Uygulama arka planda [timeoutMs] veya daha uzun kaldıysa yeniden kilitlenir. */
    fun shouldRelock(backgroundedAtMs: Long, nowMs: Long, timeoutMs: Long): Boolean =
        nowMs - backgroundedAtMs >= timeoutMs
}
