package com.mobilecomputing.app

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatDistanceTest {
    @Test
    fun shortDistanceIsShownInMeters() {
        assertEquals("250 m", formatDistance(250.4f))
    }

    @Test
    fun longDistanceIsShownInKilometers() {
        assertEquals("1,5 km", formatDistance(1500f))
    }
}
