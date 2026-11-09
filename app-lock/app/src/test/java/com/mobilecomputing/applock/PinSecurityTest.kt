package com.mobilecomputing.applock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinSecurityTest {

    @Test
    fun hash_matchesOnlyTheSamePin() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("2580", salt)
        assertTrue(PinHasher.matches("2580", salt, hash))
        assertFalse(PinHasher.matches("2581", salt, hash))
    }

    @Test
    fun hash_differsWithDifferentSalt() {
        val a = PinHasher.hash("2580", PinHasher.newSalt())
        val b = PinHasher.hash("2580", PinHasher.newSalt())
        assertFalse(a.contentEquals(b))
    }

    @Test
    fun pinRules_rejectsSimplePins() {
        listOf("1111", "0000", "1234", "4321", "6789").forEach {
            assertTrue("$it basit sayılmalı", PinRules.isTooSimple(it))
        }
        listOf("2580", "1357", "9024").forEach {
            assertFalse("$it basit sayılmamalı", PinRules.isTooSimple(it))
        }
    }

    @Test
    fun pinRules_format() {
        assertTrue(PinRules.isValidFormat("0420"))
        assertFalse(PinRules.isValidFormat("123"))
        assertFalse(PinRules.isValidFormat("12a4"))
    }

    @Test
    fun lockout_startsAfterFreeAttemptsAndDoubles() {
        assertEquals(0L, LockPolicy.lockoutDurationMs(4))
        assertEquals(30_000L, LockPolicy.lockoutDurationMs(5))
        assertEquals(60_000L, LockPolicy.lockoutDurationMs(6))
        assertEquals(120_000L, LockPolicy.lockoutDurationMs(7))
        assertEquals(LockPolicy.MAX_LOCKOUT_MS, LockPolicy.lockoutDurationMs(50))
    }

    @Test
    fun relock_respectsTimeout() {
        assertTrue(LockPolicy.shouldRelock(backgroundedAtMs = 1_000, nowMs = 1_000, timeoutMs = 0))
        assertFalse(LockPolicy.shouldRelock(backgroundedAtMs = 1_000, nowMs = 20_000, timeoutMs = 30_000))
        assertTrue(LockPolicy.shouldRelock(backgroundedAtMs = 1_000, nowMs = 31_000, timeoutMs = 30_000))
    }
}
