package com.mobilecomputing.app

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

// GPS ve ağ sağlayıcılarından anlık konumu dinler. İzin yoksa null döner.
@SuppressLint("MissingPermission")
@Composable
fun rememberCurrentLocation(hasPermission: Boolean): Location? {
    val context = LocalContext.current
    var location by remember { mutableStateOf<Location?>(null) }

    DisposableEffect(hasPermission) {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = LocationListener { location = it }
        if (hasPermission) {
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                .filter { manager.isProviderEnabled(it) }
            location = providers.mapNotNull { manager.getLastKnownLocation(it) }.maxByOrNull { it.time }
            providers.forEach { manager.requestLocationUpdates(it, 2000L, 0f, listener) }
        }
        onDispose { manager.removeUpdates(listener) }
    }
    return location
}
