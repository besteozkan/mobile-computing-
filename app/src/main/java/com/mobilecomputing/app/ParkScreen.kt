package com.mobilecomputing.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParkScreen() {
    val context = LocalContext.current
    val store = remember { ParkingStore(context) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> hasPermission = result.values.any { it } }

    val current = rememberCurrentLocation(hasPermission)
    var spot by remember { mutableStateOf(store.load()) }
    var savedPhoto by remember { mutableStateOf(store.loadPhoto()) }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Text(
            "Arabam Nerede?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        ParkMap(current = current, spot = spot, modifier = Modifier.fillMaxWidth().weight(1f))

        Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val saved = spot
                when {
                    !hasPermission -> {
                        Text("Park yerini kaydetmek için konum izni gerekiyor.")
                        Button(onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }) { Text("Konum izni ver") }
                    }

                    saved == null -> NewParkingPanel(current) { note, photo ->
                        current?.let {
                            spot = store.save(it.latitude, it.longitude, note, photo)
                            savedPhoto = photo
                        }
                    }

                    else -> SavedParkingPanel(saved, savedPhoto, current) {
                        store.clear()
                        spot = null
                        savedPhoto = null
                    }
                }
            }
        }
    }
}

@Composable
private fun NewParkingPanel(current: Location?, onPark: (String, Bitmap?) -> Unit) {
    var note by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> if (bitmap != null) photo = bitmap }

    Text(if (current == null) "Konum bekleniyor..." else "Konumun hazır.")
    OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = { Text("Not (ör. B2 katı, 14 numara)") },
        modifier = Modifier.fillMaxWidth()
    )
    photo?.let { PhotoPreview(it) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { cameraLauncher.launch(null) }) {
            Text(if (photo == null) "Fotoğraf çek" else "Yeniden çek")
        }
        Button(onClick = { onPark(note.trim(), photo) }, enabled = current != null) {
            Text("Park ettim")
        }
    }
}

@Composable
private fun SavedParkingPanel(
    spot: ParkingSpot,
    photo: Bitmap?,
    current: Location?,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    val time = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr")).format(Date(spot.time))

    Text("Park zamanı: $time")
    current?.let {
        val result = FloatArray(1)
        Location.distanceBetween(it.latitude, it.longitude, spot.latitude, spot.longitude, result)
        Text("Arabaya uzaklık: ${formatDistance(result[0])}")
    }
    if (spot.note.isNotEmpty()) Text("Not: ${spot.note}")
    photo?.let { PhotoPreview(it) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            val uri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=${spot.latitude},${spot.longitude}&travelmode=walking"
            )
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }) { Text("Yol tarifi") }
        OutlinedButton(onClick = onClear) { Text("Arabamı aldım") }
    }
}

@Composable
private fun PhotoPreview(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Park yeri fotoğrafı",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().height(120.dp)
    )
}

fun formatDistance(meters: Float): String =
    if (meters < 1000) "${meters.toInt()} m" else String.format(Locale("tr"), "%.1f km", meters / 1000)
