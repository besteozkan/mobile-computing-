package com.mobilecomputing.app

import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

// OpenStreetMap haritası: mavi nokta kullanıcı, kırmızı işaret park yeri.
@Composable
fun ParkMap(current: Location?, spot: ParkingSpot?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mapView = remember {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
        }
    }
    val meMarker = remember {
        Marker(mapView).apply {
            title = "Ben"
            icon = ContextCompat.getDrawable(context, R.drawable.me_dot)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    }
    val carMarker = remember {
        Marker(mapView).apply {
            title = "Arabam"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
    }
    // Harita ilk konum geldiğinde ve park yeri değiştiğinde ortalanır.
    val lastCentered = remember { arrayOfNulls<Any>(1) }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier, update = { map ->
        map.overlays.clear()
        current?.let {
            meMarker.position = GeoPoint(it.latitude, it.longitude)
            map.overlays.add(meMarker)
        }
        spot?.let {
            carMarker.position = GeoPoint(it.latitude, it.longitude)
            carMarker.snippet = it.note
            map.overlays.add(carMarker)
        }

        val centerKey: Any? = spot ?: current?.let { "me" }
        if (centerKey != null && centerKey != lastCentered[0]) {
            lastCentered[0] = centerKey
            map.controller.animateTo(if (spot != null) carMarker.position else meMarker.position)
        }
        map.invalidate()
    })
}
